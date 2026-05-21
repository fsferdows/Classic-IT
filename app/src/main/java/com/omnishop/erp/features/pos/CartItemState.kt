package com.omnishop.erp.features.pos

import com.omnishop.erp.core.data.local.ProductEntity

data class CartItemState(
    val product: ProductEntity,
    val quantity: Int
)
