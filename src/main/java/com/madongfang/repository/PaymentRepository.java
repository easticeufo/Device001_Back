package com.madongfang.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.madongfang.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

}
