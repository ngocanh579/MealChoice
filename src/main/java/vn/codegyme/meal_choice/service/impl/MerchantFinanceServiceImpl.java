package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.codegyme.meal_choice.repository.MerchantPayoutRequestRepository;
import vn.codegyme.meal_choice.service.MerchantFinanceService;
import vn.codegyme.meal_choice.service.MerchantStatService;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantFinanceServiceImpl
        implements MerchantFinanceService {

    private final MerchantStatService merchantStatService;

    private final MerchantPayoutRequestRepository
            payoutRequestRepository;


    // ==========================================
    // TỔNG DOANH THU
    // ==========================================

    @Override
    public BigDecimal getTotalRevenue(UUID merchantId) {

        return merchantStatService
                .getTotalRevenue(merchantId);
    }


    // ==========================================
    // TỔNG TIỀN ĐÃ ĐƯỢC ADMIN CHUYỂN
    // ==========================================

    @Override
    public BigDecimal getTotalPaidAmount(UUID merchantId) {

        BigDecimal amount =
                payoutRequestRepository
                        .getTotalPaidAmount(merchantId);

        return amount != null
                ? amount
                : BigDecimal.ZERO;
    }


    // ==========================================
    // SỐ DƯ CÓ THỂ RÚT
    // ==========================================

    @Override
    public BigDecimal getAvailableBalance(UUID merchantId) {

        BigDecimal totalRevenue =
                getTotalRevenue(merchantId);

        BigDecimal totalPaid =
                getTotalPaidAmount(merchantId);

        BigDecimal balance =
                totalRevenue.subtract(totalPaid);


        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }

        return balance;
    }
}
