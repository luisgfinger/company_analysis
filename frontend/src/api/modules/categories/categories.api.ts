import type { RequestConfig, HttpClient } from "../../core/types";
import type { Category } from "./categories.types";

const CATEGORIES_RESOURCE = "/api/v1/categories";

export interface CategoriesApi {
  findAll(config?: RequestConfig): Promise<Category[]>;
}

export function createCategoriesApi(httpClient: HttpClient): CategoriesApi {
  return {
    findAll(config) {
      return httpClient.get<Category[]>(CATEGORIES_RESOURCE, config);
    },
  };
}
