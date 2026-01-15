package com.cdac.hpuat.inventory.entity.entityids;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HsttDrugMstPK implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "hstnum_item_id")
    private Integer hstnumItemId;

    @Column(name = "gnum_hospital_code")
    private Integer gnumHospitalCode;
}
