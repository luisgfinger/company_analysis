import type { RequestConfig, HttpClient } from "../../core/types";
import type { ApiPage } from "../../shared/pagination.types";
import type {
  ExportProductsPdfRequest,
  FindAllProductsParams,
  FindPagedProductsParams,
  Product,
} from "./products.types";

const PRODUCTS_RESOURCE = "/api/v1/products";

type ApiProduct = Omit<Product, "barcode"> & {
  barcode: string | null;
};

function normalizeBarcode(value: string | null | undefined): string {
  return typeof value === "string" ? value : "";
}

function normalizeProduct(product: ApiProduct): Product {
  return {
    ...product,
    barcode: normalizeBarcode(product.barcode),
  };
}

function normalizeSortParam(sort: string): string {
  return sort
    .split(",")
    .map((value) => value.trim())
    .join(",");
}

function normalizeProductsQueryParams<
  TParams extends FindAllProductsParams | FindPagedProductsParams,
>(params?: TParams): TParams | undefined {
  if (!params?.sort) {
    return params;
  }

  return {
    ...params,
    sort: Array.isArray(params.sort)
      ? params.sort.map(normalizeSortParam)
      : normalizeSortParam(params.sort),
  };
}

function normalizeProductPage(page: ApiPage<ApiProduct>): ApiPage<Product> {
  return {
    ...page,
    content: page.content.map(normalizeProduct),
  };
}

export interface ProductsApi {
  exportPdf(
    payload: ExportProductsPdfRequest,
    config?: RequestConfig,
  ): Promise<Blob>;
  findAll(
    params?: FindAllProductsParams,
    config?: RequestConfig,
  ): Promise<Product[]>;
  findById(id: number, config?: RequestConfig): Promise<Product>;
  findPaged(
    params?: FindPagedProductsParams,
    config?: RequestConfig,
  ): Promise<ApiPage<Product>>;
}

export function createProductsApi(httpClient: HttpClient): ProductsApi {
  return {
    exportPdf(payload, config) {
      const headers = new Headers(config?.headers);

      headers.set("Accept", "application/pdf");

      return httpClient.post<Blob, ExportProductsPdfRequest>(
        `${PRODUCTS_RESOURCE}/export/pdf`,
        {
          ...config,
          body: payload,
          headers,
          responseType: "blob",
        },
      );
    },
    findAll(params, config) {
      return httpClient
        .get<ApiProduct[], FindAllProductsParams>(
        `${PRODUCTS_RESOURCE}/all`,
        {
          ...config,
          query: normalizeProductsQueryParams(params),
        },
      )
        .then((products) => products.map(normalizeProduct));
    },
    findById(id, config) {
      return httpClient
        .get<ApiProduct>(`${PRODUCTS_RESOURCE}/${id}`, config)
        .then(normalizeProduct);
    },
    findPaged(params, config) {
      return httpClient
        .get<ApiPage<ApiProduct>, FindPagedProductsParams>(
        PRODUCTS_RESOURCE,
        {
          ...config,
          query: normalizeProductsQueryParams(params),
        },
      )
        .then(normalizeProductPage);
    },
  };
}
