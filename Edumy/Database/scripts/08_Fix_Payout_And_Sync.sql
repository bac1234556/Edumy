-- 08_Fix_Payout_And_Sync.sql
-- Idempotent migration script for SQL Server:
-- 1. Enforce at most 1 open ('requested') withdrawal request per instructor
-- 2. Backfill matures_at = DATEADD(DAY, 7, earned_at) for unmatured payout entries

SET QUOTED_IDENTIFIER ON;
GO

IF OBJECT_ID('dbo.payout_withdrawals', 'U') IS NOT NULL
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes
        WHERE name = 'UX_WithdrawalRequests_OpenPerInstructor'
          AND object_id = OBJECT_ID('dbo.payout_withdrawals')
    )
    BEGIN
        CREATE UNIQUE NONCLUSTERED INDEX UX_WithdrawalRequests_OpenPerInstructor
            ON dbo.payout_withdrawals (instructor_id)
            WHERE status = 'requested';
    END
END
GO

IF OBJECT_ID('dbo.payout_earnings', 'U') IS NOT NULL
BEGIN
    UPDATE dbo.payout_earnings
    SET matures_at = DATEADD(DAY, 7, earned_at)
    WHERE earned_at IS NOT NULL
      AND (matures_at IS NULL OR matures_at > GETDATE());
END
GO
