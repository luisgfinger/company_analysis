export interface Product {
  id: number;
  code: string;
  name: string;
  price: number;
  cost: number;
  profitMargin: number;
  category: string;
  barcode: string;
}

export type ProductPdfExportItem = Omit<Product, "id">;

export interface ExportProductsPdfRequest {
  title: string;
  products: ProductPdfExportItem[];
}

export interface ProductFilters {
  barcode?: string;
  category?: string;
  code?: string;
  q?: string;
}

export type SortDirection = "asc" | "desc";
export type ProductSortField =
  | "barcode"
  | "category"
  | "code"
  | "id"
  | "name"
  | "cost"
  | "profitMargin"
  | "price";
export type ProductSortParam = `${ProductSortField},${SortDirection}`;

export interface FindPagedProductsParams extends ProductFilters {
  page?: number;
  size?: number;
  sort?: ProductSortParam | ProductSortParam[];
}

export interface FindAllProductsParams extends ProductFilters {
  sort?: ProductSortParam | ProductSortParam[];
}
