package com.edumy.payout.repository;

import com.edumy.payout.model.PayoutAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayoutAccountRepository extends JpaRepository<PayoutAccount, Integer> {
}
