package vn.codegyme.meal_choice.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codegyme.meal_choice.entity.Merchant;
import vn.codegyme.meal_choice.entity.OrderStatus;
import vn.codegyme.meal_choice.repository.MerchantRepository;
import vn.codegyme.meal_choice.repository.OrderRepository;
import vn.codegyme.meal_choice.service.TrustedPartnerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TrustedPartnerServiceImpl implements TrustedPartnerService {

    private static final BigDecimal MIN_REVENUE =
            new BigDecimal("100000000");

    private final MerchantRepository merchantRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void registerTrustedPartner(UUID merchantId) {

        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Merchant"));

        if (merchant.isTrustedPartner()) {
            throw new RuntimeException(
                    "Merchant đã là đối tác thân thiết"
            );
        }

        YearMonth currentMonth = YearMonth.now();

        LocalDateTime startDate = currentMonth
                .atDay(1)
                .atStartOfDay();

        LocalDateTime endDate = currentMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay();

        BigDecimal revenue = orderRepository.calculateRevenue(
                merchantId,
                OrderStatus.COMPLETED,
                startDate,
                endDate
        );

        if (revenue.compareTo(MIN_REVENUE) < 0) {
            throw new RuntimeException(
                    "Doanh thu tháng chưa đạt 100.000.000 VNĐ"
            );
        }

        merchant.setTrustedPartner(true);

        merchantRepository.save(merchant);
    }
}