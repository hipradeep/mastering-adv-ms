package com.hipradeep.paymentservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "USER_BALANCE_TBL")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBalance {
    @Id
    private Integer userId;
    private Integer price;
}
