import type { HttpClient } from "./core/types";
import { createCategoriesApi, type CategoriesApi } from "./modules/categories";
import { createInventoryApi, type InventoryApi } from "./modules/inventory";
import { createProductsApi, type ProductsApi } from "./modules/products";

export interface Api {
  categories: CategoriesApi;
  inventory: InventoryApi;
  products: ProductsApi;
}

export function createApi(httpClient: HttpClient): Api {
  return {
    categories: createCategoriesApi(httpClient),
    inventory: createInventoryApi(httpClient),
    products: createProductsApi(httpClient),
  };
}
