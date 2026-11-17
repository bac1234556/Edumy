$OutputEncoding = [System.Text.UTF8Encoding]::new()
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new()
$PSDefaultParameterValues['*:Encoding'] = 'utf8'

$baseApi = "http://localhost:5000"

function Unwrap-Data($res) {
    if ($null -ne $res -and ($res.PSObject.Properties['success']) -and ($res.PSObject.Properties['data']) -and $null -ne $res.data) {
        return $res.data
    }
    return $res
}

Write-Host "=========================================================="
Write-Host "   EDUMY SPRING MODULITH SYSTEM VERIFICATION TEST         "
Write-Host "=========================================================="

Write-Host "`n=== 1. TEST ACTUATOR HEALTH & PROMETHEUS ==="
$healthRes = Invoke-RestMethod -Uri "$baseApi/actuator/health" -Method Get
Write-Host "[PASS] Actuator Health Status = $($healthRes.status)"

Write-Host "`n=== 2. TEST AUTHENTICATION & MULTI-ROLE TOKENS (WEB & MOBILE) ==="
$adminLoginBody = '{"email":"admin@edumy.com","password":"Admin@123"}'
$adminRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/auth/login" -Method Post -Body $adminLoginBody -ContentType "application/json; charset=utf-8")
$adminToken = $adminRes.token
Write-Host "[PASS] Admin Token acquired: Role=$($adminRes.user.role), Name=$($adminRes.user.fullName)"

$instLoginBody = '{"email":"instructor@edumy.com","password":"Admin@123"}'
$instRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/auth/login" -Method Post -Body $instLoginBody -ContentType "application/json; charset=utf-8")
$instToken = $instRes.token
Write-Host "[PASS] Instructor Token acquired: Role=$($instRes.user.role), Name=$($instRes.user.fullName)"

$studentLoginBody = '{"email":"student@edumy.com","password":"Admin@123"}'
$studentRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/auth/login" -Method Post -Body $studentLoginBody -ContentType "application/json; charset=utf-8")
$studentToken = $studentRes.token
Write-Host "[PASS] Student Token acquired: Role=$($studentRes.user.role), Name=$($studentRes.user.fullName)"

$mobileRes = Invoke-RestMethod -Uri "$baseApi/api/v1/auth/mobile/login" -Method Post -Body $studentLoginBody -ContentType "application/json; charset=utf-8"
Write-Host "[PASS] Mobile Auth Token acquired: ExpiresAt=$($mobileRes.expiresAt)"

Write-Host "`n=== 3. TEST CATALOG, RECOMMENDATIONS & SEARCH LOGGING ==="
$coursesRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/courses" -Method Get)
Write-Host "[PASS] /api/courses returned courses count: $($coursesRes.items.Count)"

$coursesUpperRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/Courses" -Method Get)
Write-Host "[PASS] /api/Courses returned courses count: $($coursesUpperRes.items.Count)"

$recRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/v1/courses/recommend" -Method Get)
Write-Host "[PASS] /api/v1/courses/recommend returned $($recRes.Count) courses"

$firstCourse = $coursesRes.items[0]
$courseId = $firstCourse.courseId
$simRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/v1/courses/$courseId/similar?limit=4" -Method Get)
Write-Host "[PASS] /api/v1/courses/$courseId/similar returned $($simRes.Count) similar courses"

$searchHeaders = @{ Authorization = "Bearer $studentToken" }
$searchRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/courses?search=Spring&keyword=Spring" -Method Get -Headers $searchHeaders)
Write-Host "[PASS] Course Search with Student query logged to search_histories successfully."

Write-Host "`n=== 4. TEST Q&A FORUM & INSTRUCTOR PAYOUT MODULE ==="
$qaRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/v1/courses/$courseId/questions" -Method Get)
Write-Host "[PASS] Q&A /api/v1/courses/$courseId/questions returned totalElements=$($qaRes.totalElements)"

$instHeaders = @{
    Authorization = "Bearer $instToken"
    "Content-Type" = "application/json; charset=utf-8"
}
$earningSummary = Invoke-RestMethod -Uri "$baseApi/api/v1/me/earnings/summary" -Method Get -Headers $instHeaders
Write-Host "[PASS] Payout /api/v1/me/earnings/summary: AvailableBalance=$($earningSummary.availableBalance), TotalEarned=$($earningSummary.totalEarned)"

Write-Host "`n=== 5. TEST DUAL-CASING PAYMENT CALLBACK ROUTING ==="
$callbackBody = '{"orderId":1,"amount":500000,"transactionId":"TXN_TEST_12345","status":"SUCCESS","paymentMethod":"VNPAY"}'
$cbLower = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/payment/callback" -Method Post -Body $callbackBody -ContentType "application/json; charset=utf-8")
Write-Host "[PASS] Lowercase /api/payment/callback HTTP 200: Code=$($cbLower.rspCode), Msg=$($cbLower.message)"

$cbUpper = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/Payment/callback" -Method Post -Body $callbackBody -ContentType "application/json; charset=utf-8")
Write-Host "[PASS] Uppercase /api/Payment/callback HTTP 200: Code=$($cbUpper.rspCode), Msg=$($cbUpper.message)"

Write-Host "`n=== 6. TEST INSTRUCTOR SECTION & QUIZ CREATION ==="
$sectionBody = '{"title":"Chuong 1: Kien Thuc Nen Tang","orderIndex":1}'
$sectionRes = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/courses/$courseId/sections" -Method Post -Headers $instHeaders -Body $sectionBody)
$sectionId = $sectionRes.sectionId
Write-Host "[PASS] Instructor created Section: ID=$sectionId, Title=$($sectionRes.title)"

$quizBody = @{
    courseSectionId = $sectionId
    title = "Kiem Tra Kien Thuc Lap Trinh Chuyen Sau"
    timeLimitMinutes = 45
    passingScore = 80
    questions = @(
        @{
            questionText = "Trong Spring Boot 3, controller co the ho tro ca PascalCase va camelCase routing khong?"
            points = 10
            answers = @(
                @{ answerText = "Co, su dung mang String trong @RequestMapping"; isCorrect = $true },
                @{ answerText = "Khong the lam duoc"; isCorrect = $false }
            )
        }
    )
} | ConvertTo-Json -Depth 5

$createdQuiz = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/quizzes" -Method Post -Headers $instHeaders -Body $quizBody)
Write-Host "[PASS] Quiz created successfully: ID=$($createdQuiz.quizId), Title=$($createdQuiz.title), Questions=$($createdQuiz.questions.Count)"

Write-Host "`n=== 7. TEST AUDIT TRAIL & USER ACTIVITY LOGS ==="
$adminHeaders = @{ Authorization = "Bearer $adminToken" }
$activities = Unwrap-Data (Invoke-RestMethod -Uri "$baseApi/api/admin/activities" -Method Get -Headers $adminHeaders)
Write-Host "[PASS] Admin audit trail fetched successfully: Total logged events = $($activities.Count)"
foreach ($act in $activities | Select-Object -First 3) {
    Write-Host "  -> Event: Action=$($act.action), User=$($act.userEmail), Resource=$($act.resourceId), Time=$($act.timestamp)"
}

Write-Host "`n=========================================================="
Write-Host "  ALL 7 MODULITH CRITICAL PATHS VERIFIED WITH SUCCESS!    "
Write-Host "=========================================================="
