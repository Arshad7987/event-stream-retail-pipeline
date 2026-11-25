package com.arshadansari.retailpipeline.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;

public interface RetailTransactionRepository extends JpaRepository<RetailTransaction, String> {

    @Query("""
            select count(t) from RetailTransaction t
            where t.storeId = :storeId and t.occurredAt >= :from and t.occurredAt < :to
            """)
    long countForStoreBetween(@Param("storeId") String storeId,
                              @Param("from") Instant from,
                              @Param("to") Instant to);

    @Query("""
            select coalesce(sum(t.amount), 0) from RetailTransaction t
            where t.storeId = :storeId and t.occurredAt >= :from and t.occurredAt < :to
            """)
    BigDecimal sumAmountForStoreBetween(@Param("storeId") String storeId,
                                        @Param("from") Instant from,
                                        @Param("to") Instant to);

    @Query("""
            select coalesce(sum(t.itemCount), 0) from RetailTransaction t
            where t.storeId = :storeId and t.occurredAt >= :from and t.occurredAt < :to
            """)
    long sumItemsForStoreBetween(@Param("storeId") String storeId,
                                 @Param("from") Instant from,
                                 @Param("to") Instant to);
}
