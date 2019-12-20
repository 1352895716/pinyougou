package com.zlp.common;

import java.io.Serializable;

/**
 * @ClassName: OrderRecord
 * @Description: TODO
 * @Autor:13528
 * @Date: 2019/12/19 16:13
 * @Version 1.0
 **/
public class OrderRecord implements Serializable {
    private Long id;
    private String userId;

    public OrderRecord(Long id, String userId) {
        this.id = id;
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
