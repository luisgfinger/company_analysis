import type { RequestConfig, HttpClient } from "../../core/types";
import type { ApiPage } from "../../shared/pagination.types";
import type {
  FindAllInventoryParams,
  FindPagedInventoryParams,
  Inventory,
} from "./inventory.types";

const INVENTORY_RESOURCE = "/api/v1/inventory";

type ApiInventory = Omit<Inventory, "productBarcode" | "quantityInStock"> & {
  productBarcode: string | null;
  quantityInStock: number | string | null;
};

function normalizeBarcode(value: string | null | undefined): string {
  return typeof value === "string" ? value : "";
}

function normalizeQuantity(
  value: number | string | null | undefined,
): number | null {
  if (typeof value === "number" && Number.isFinite(value)) {
    return value;
  }

  if (typeof value === "string") {
    const parsedValue = Number(value);

    return Number.isFinite(parsedValue) ? parsedValue : null;
  }

  return null;
}

function normalizeInventory(inventory: ApiInventory): Inventory {
  return {
    ...inventory,
    productBarcode: normalizeBarcode(inventory.productBarcode),
    quantityInStock: normalizeQuantity(inventory.quantityInStock),
  };
}

function normalizeSortParam(sort: string): string {
  return sort
    .split(",")
    .map((value) => value.trim())
    .join(",");
}

function normalizeInventoryQueryParams<
  TParams extends FindAllInventoryParams | FindPagedInventoryParams,
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

function normalizeInventoryPage(
  page: ApiPage<ApiInventory>,
): ApiPage<Inventory> {
  return {
    ...page,
    content: page.content.map(normalizeInventory),
  };
}

export interface InventoryApi {
  findAll(
    params?: FindAllInventoryParams,
    config?: RequestConfig,
  ): Promise<Inventory[]>;
  findById(id: number, config?: RequestConfig): Promise<Inventory>;
  findPaged(
    params?: FindPagedInventoryParams,
    config?: RequestConfig,
  ): Promise<ApiPage<Inventory>>;
}

export function createInventoryApi(httpClient: HttpClient): InventoryApi {
  return {
    findAll(params, config) {
      return httpClient
        .get<ApiInventory[], FindAllInventoryParams>(
          `${INVENTORY_RESOURCE}/all`,
          {
            ...config,
            query: normalizeInventoryQueryParams(params),
          },
        )
        .then((inventory) => inventory.map(normalizeInventory));
    },
    findById(id, config) {
      return httpClient
        .get<ApiInventory>(`${INVENTORY_RESOURCE}/${id}`, config)
        .then(normalizeInventory);
    },
    findPaged(params, config) {
      return httpClient
        .get<ApiPage<ApiInventory>, FindPagedInventoryParams>(
          INVENTORY_RESOURCE,
          {
            ...config,
            query: normalizeInventoryQueryParams(params),
          },
        )
        .then(normalizeInventoryPage);
    },
  };
}
