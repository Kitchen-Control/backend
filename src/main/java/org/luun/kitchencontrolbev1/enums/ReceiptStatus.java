package org.luun.kitchencontrolbev1.enums;

public enum ReceiptStatus {
    DRAFT, //Trong database dữ liệu vẫn là DRAFT nên giữ nguyên tránh lỗi
    READY,
    COMPLETED
}
