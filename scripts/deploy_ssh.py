import argparse
import os
import sys
import glob
import time
import paramiko

def print_log(msg):
    print(f"[SSH-DEPLOY] {msg}", flush=True)

def main():
    parser = argparse.ArgumentParser(description="Automated SSH Deploy Script for Jenkins")
    parser.add_argument("--host", default="192.168.60.165", help="Target Server Host/IP")
    parser.add_argument("--port", type=int, default=22, help="SSH Port")
    parser.add_argument("--user", default="baclev", help="SSH Username")
    parser.add_argument("--password", default="2011", help="SSH Password (fallback)")
    parser.add_argument("--key", default=r"C:\Users\bacle\.ssh\id_ed25519", help="SSH Private Key path")
    parser.add_argument("--server-dir", default="/home/baclev/Edumy/Edumy", help="Remote Project Dir")
    parser.add_argument("--local-dir", default=".", help="Local workspace dir")
    args = parser.parse_args()

    print_log(f"Dang ket noi SSH toi {args.user}@{args.host}:{args.port}...")

    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())

    # Try connecting with private key first, fallback to password
    connected = False
    if os.path.exists(args.key):
        try:
            print_log(f"Thu ket noi bang SSH Private Key: {args.key}")
            k = paramiko.Ed25519Key.from_private_key_file(args.key)
            ssh.connect(args.host, port=args.port, username=args.user, pkey=k, timeout=10)
            connected = True
            print_log("Ket noi SSH bang Private Key thanh cong!")
        except Exception as e:
            print_log(f"Ket noi bang Key that bai ({e}), thu ket noi bang Password...")

    if not connected:
        try:
            ssh.connect(args.host, port=args.port, username=args.user, password=args.password, timeout=10)
            connected = True
            print_log("Ket noi SSH bang Password thanh cong!")
        except Exception as e:
            print_log(f"LOI: Khong the ket noi SSH: {e}")
            sys.exit(1)

    sftp = ssh.open_sftp()

    def run_remote(cmd):
        print_log(f">>> Thuc thi tu xa: {cmd}")
        stdin, stdout, stderr = ssh.exec_command(cmd)
        exit_status = stdout.channel.recv_exit_status()
        out = stdout.read().decode('utf-8', errors='ignore')
        err = stderr.read().decode('utf-8', errors='ignore')
        if out:
            print(out, flush=True)
        if err and exit_status != 0:
            print(f"[STDERR] {err}", flush=True)
        return exit_status

    # 1. Pull latest code on server
    print_log("[1/4] Dong bo code moi nhat qua Git tren server...")
    run_remote(f"cd '{args.server_dir}' && git pull origin main || true")

    # 2. Upload Backend JAR
    print_log("[2/4] Sao chep ban build Backend (JAR) sang server...")
    run_remote(f"mkdir -p '{args.server_dir}/Backend/target'")
    
    # Search local jar
    search_paths = [
        os.path.join(args.local_dir, "Edumy", "Backend", "target", "*.jar"),
        os.path.join(args.local_dir, "Backend", "target", "*.jar"),
        r"C:\Users\bacle\Downloads\Edumy-Project\Edumy-main\Edumy\Backend\target\*.jar"
    ]
    jar_files = []
    for p in search_paths:
        jar_files.extend(glob.glob(p))
    
    jar_files = [f for f in jar_files if not f.endswith("-sources.jar") and not f.endswith("-javadoc.jar")]
    if jar_files:
        local_jar = jar_files[0]
        remote_jar = f"{args.server_dir}/Backend/target/{os.path.basename(local_jar)}"
        print_log(f"Dang upload: {local_jar} -> {remote_jar}")
        sftp.put(local_jar, remote_jar)
        print_log("Upload JAR thanh cong!")
    else:
        print_log("[CANH BAO] Khong tim thay file JAR de upload, server se dung ban cu neu co.")

    # 3. Upload Frontend dist
    print_log("[3/4] Sao chep ban build Frontend (dist) sang server...")
    dist_paths = [
        os.path.join(args.local_dir, "Edumy", "Frontend", "dist"),
        os.path.join(args.local_dir, "Frontend", "dist"),
        r"C:\Users\bacle\Downloads\Edumy-Project\Edumy-main\Edumy\Frontend\dist"
    ]
    local_dist = None
    for p in dist_paths:
        if os.path.exists(p) and os.path.isdir(p):
            local_dist = p
            break

    if local_dist:
        run_remote(f"mkdir -p '{args.server_dir}/Frontend/dist'")
        for root, dirs, files in os.walk(local_dist):
            rel_dir = os.path.relpath(root, local_dist).replace("\\", "/")
            if rel_dir == ".":
                remote_sub = f"{args.server_dir}/Frontend/dist"
            else:
                remote_sub = f"{args.server_dir}/Frontend/dist/{rel_dir}"
                run_remote(f"mkdir -p '{remote_sub}'")
            for file in files:
                l_f = os.path.join(root, file)
                r_f = f"{remote_sub}/{file}"
                sftp.put(l_f, r_f)
        print_log("Upload Frontend dist thanh cong!")
        run_remote(f"chmod -R 755 '{args.server_dir}/Frontend/dist'")
    else:
        print_log("[CANH BAO] Khong tim thay thu muc Frontend dist de upload.")

    sftp.close()

    # 4. Run Docker Compose
    print_log("[4/4] Khoi dong cac dich vu bang Docker Compose qua SSH...")
    status = run_remote(f"cd '{args.server_dir}' && docker compose -p edumy up -d --build && docker compose -p edumy ps")
    
    ssh.close()
    if status == 0:
        print_log("TRIEN KHAI QUA SSH THANH CONG 100%!")
        sys.exit(0)
    else:
        print_log(f"TRIEN KHAI THAT BAI VOI MA LOI: {status}")
        sys.exit(status)

if __name__ == "__main__":
    main()
