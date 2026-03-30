export interface Inventory {
  id: number;
  productId: number;
  productCode: string;
  productName: string;
  productBarcode: string;
  category: string;
  quantityInStock: number | null;
}

export interface InventoryFilters {
  barcode?: string;
  category?: string;
  code?: string;
  q?: string;
}

export type InventorySortField =
  | "category"
  | "id"
  | "product.barcode"
  | "product.code"
  | "product.name"
  | "quantityInStock";

export type InventorySortParam =
  `${InventorySortField},${"asc" | "desc"}`;

export interface FindPagedInventoryParams extends InventoryFilters {
  page?: number;
  size?: number;
  sort?: InventorySortParam | InventorySortParam[];
}

export interface FindAllInventoryParams extends InventoryFilters {
  sort?: InventorySortParam | InventorySortParam[];
}
