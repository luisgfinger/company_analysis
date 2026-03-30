import { startTransition, useEffect, useState } from "react";

import {
  api,
  type ApiPage,
  type ApiSortState,
  type Category as ProductCategory,
  type Inventory as InventoryItem,
  type Product as ProductItem,
  type ProductSortParam,
} from "../../api";
import type { SelectOption } from "../common/SelectField";
import { Pagination } from "../common/Pagination";
import {
  ProductFilters,
  type ProductFilterFormValues,
} from "./product/ProductFilters";
import { ProductList } from "./product/ProductList";
import { ProductUtilityQueries } from "./product/ProductUtilityQueries";
import {
  DEFAULT_PAGE_SIZE,
  DEFAULT_SORT,
  PRODUCT_PAGINATION_SIBLING_COUNT,
  PRODUCT_SECONDARY_ACTION_BUTTON_CLASSES,
} from "./product/product.constants";
import {
  expandCategorySelections,
  getProductUtilityQuery,
  getUtilityQueryExcludedCategories,
  type ProductUtilityQuery,
  type ProductUtilityQueryId,
} from "./product/product.utility-queries";
import type { ProductListItem } from "./product/product.types";

const FILTER_AUTO_APPLY_DELAY_MS = 300;
const POSITIVE_COST_SORT: ProductSortParam = "cost,asc";
const DEFAULT_REPORT_TITLE = "Relatorio de Produtos";
const CLIENT_PAGE_SORT_STATE: ApiSortState = {
  empty: false,
  sorted: true,
  unsorted: false,
};

interface ProductQueryState {
  q: string;
  code: string;
  barcode: string;
  category: string[];
  excludedCategories: string[];
  sort: ProductSortParam;
  size: number;
}

const DEFAULT_QUERY_STATE: ProductQueryState = {
  q: "",
  code: "",
  barcode: "",
  category: [],
  excludedCategories: [],
  sort: DEFAULT_SORT,
  size: DEFAULT_PAGE_SIZE,
};

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (error instanceof Error && error.message.trim()) {
    return error.message;
  }

  return fallbackMessage;
}

function toFormValues(query: ProductQueryState): ProductFilterFormValues {
  return {
    q: query.q,
    code: query.code,
    barcode: query.barcode,
    category: [...query.category],
    excludedCategories: [...query.excludedCategories],
    sort: query.sort,
    size: String(query.size),
  };
}

function normalizeText(value: string): string {
  return value.trim();
}

function normalizeCategories(values: readonly string[]): string[] {
  const uniqueValues = new Set<string>();

  for (const value of values) {
    const normalizedValue = normalizeText(value);

    if (normalizedValue) {
      uniqueValues.add(normalizedValue);
    }
  }

  return [...uniqueValues].sort((left, right) => left.localeCompare(right));
}

function areSameStringLists(
  left: readonly string[],
  right: readonly string[],
): boolean {
  return (
    left.length === right.length &&
    left.every((value, index) => value === right[index])
  );
}

function removeCategories(
  values: readonly string[],
  categoriesToRemove: readonly string[],
): string[] {
  const normalizedCategoriesToRemove = new Set(
    categoriesToRemove.map((category) => category.trim().toLowerCase()),
  );

  return values.filter(
    (value) => !normalizedCategoriesToRemove.has(value.trim().toLowerCase()),
  );
}

function mergeCategories(
  values: readonly string[],
  categoriesToAdd: readonly string[],
): string[] {
  return normalizeCategories([...values, ...categoriesToAdd]);
}

function normalizeAndExpandCategories(
  values: readonly string[],
  categories: readonly ProductCategory[],
): string[] {
  const normalizedValues = normalizeCategories(values);

  if (normalizedValues.length === 0 || categories.length === 0) {
    return normalizedValues;
  }

  return normalizeCategories(expandCategorySelections(normalizedValues, categories));
}

function normalizeFormCategorySelections(
  formValues: ProductFilterFormValues,
  categories: readonly ProductCategory[],
): ProductFilterFormValues {
  const normalizedExcludedCategories = normalizeAndExpandCategories(
    formValues.excludedCategories,
    categories,
  );

  return {
    ...formValues,
    category: removeCategories(
      normalizeAndExpandCategories(formValues.category, categories),
      normalizedExcludedCategories,
    ),
    excludedCategories: normalizedExcludedCategories,
  };
}

function normalizeQuery(
  formValues: ProductFilterFormValues,
): ProductQueryState {
  const parsedSize = Number.parseInt(formValues.size, 10);

  return {
    q: normalizeText(formValues.q),
    code: normalizeText(formValues.code),
    barcode: normalizeText(formValues.barcode),
    category: normalizeCategories(formValues.category),
    excludedCategories: normalizeCategories(formValues.excludedCategories),
    sort: formValues.sort as ProductSortParam,
    size:
      Number.isFinite(parsedSize) && parsedSize > 0
        ? parsedSize
        : DEFAULT_PAGE_SIZE,
  };
}

function isSameQueryState(
  left: ProductQueryState,
  right: ProductQueryState,
): boolean {
  return (
    left.q === right.q &&
    left.code === right.code &&
    left.barcode === right.barcode &&
    areSameStringLists(left.category, right.category) &&
    areSameStringLists(left.excludedCategories, right.excludedCategories) &&
    left.sort === right.sort &&
    left.size === right.size
  );
}

function buildPagedRequest(query: ProductQueryState, page: number) {
  const selectedCategory =
    query.category.length === 1 ? query.category[0] : undefined;

  return {
    page: page - 1,
    size: query.size,
    sort: query.sort,
    ...(query.q ? { q: query.q } : {}),
    ...(query.code ? { code: query.code } : {}),
    ...(query.barcode ? { barcode: query.barcode } : {}),
    ...(selectedCategory ? { category: selectedCategory } : {}),
  };
}

function buildFindAllRequest(query: ProductQueryState) {
  const selectedCategory =
    query.category.length === 1 ? query.category[0] : undefined;

  return {
    sort: query.sort,
    ...(query.q ? { q: query.q } : {}),
    ...(query.code ? { code: query.code } : {}),
    ...(query.barcode ? { barcode: query.barcode } : {}),
    ...(selectedCategory ? { category: selectedCategory } : {}),
  };
}

function buildInventoryRequest(query: ProductQueryState) {
  const selectedCategory =
    query.category.length === 1 ? query.category[0] : undefined;

  return {
    ...(query.q ? { q: query.q } : {}),
    ...(query.code ? { code: query.code } : {}),
    ...(query.barcode ? { barcode: query.barcode } : {}),
    ...(selectedCategory ? { category: selectedCategory } : {}),
  };
}

function createInventoryByProductIdMap(
  inventory: readonly InventoryItem[],
): ReadonlyMap<number, number | null> {
  return new Map(
    inventory.map((inventoryItem) => [
      inventoryItem.productId,
      inventoryItem.quantityInStock,
    ]),
  );
}

function attachInventoryToProducts(
  products: readonly ProductItem[],
  inventory: readonly InventoryItem[],
): ProductListItem[] {
  const inventoryByProductId = createInventoryByProductIdMap(inventory);

  return products.map((product) => ({
    ...product,
    quantityInStock: inventoryByProductId.get(product.id) ?? null,
  }));
}

function attachInventoryToProductPage(
  productsPage: ApiPage<ProductItem>,
  inventory: readonly InventoryItem[],
): ApiPage<ProductListItem> {
  return {
    ...productsPage,
    content: attachInventoryToProducts(productsPage.content, inventory),
  };
}

function createClientPage<TItem>(
  products: readonly TItem[],
  page: number,
  size: number,
): ApiPage<TItem> {
  const totalElements = products.length;
  const totalPages = totalElements === 0 ? 0 : Math.ceil(totalElements / size);
  const safePageNumber =
    totalPages === 0 ? 0 : Math.min(Math.max(page - 1, 0), totalPages - 1);
  const offset = totalPages === 0 ? 0 : safePageNumber * size;
  const content = products.slice(offset, offset + size);

  return {
    content: [...content],
    empty: totalElements === 0,
    first: totalPages <= 1 || safePageNumber === 0,
    last: totalPages <= 1 || safePageNumber === totalPages - 1,
    number: safePageNumber,
    numberOfElements: content.length,
    pageable: {
      offset,
      pageNumber: safePageNumber,
      pageSize: size,
      paged: true,
      sort: CLIENT_PAGE_SORT_STATE,
      unpaged: false,
    },
    size,
    sort: CLIENT_PAGE_SORT_STATE,
    totalElements,
    totalPages,
  };
}

function compareProductsForPositiveCostSort(
  left: ProductItem,
  right: ProductItem,
): number {
  const leftHasPositiveCost = left.cost > 0;
  const rightHasPositiveCost = right.cost > 0;

  if (leftHasPositiveCost && rightHasPositiveCost) {
    const costDifference = left.cost - right.cost;

    if (costDifference !== 0) {
      return costDifference;
    }
  } else if (leftHasPositiveCost) {
    return -1;
  } else if (rightHasPositiveCost) {
    return 1;
  }

  const nameDifference = left.name.localeCompare(right.name);

  if (nameDifference !== 0) {
    return nameDifference;
  }

  const codeDifference = left.code.localeCompare(right.code);

  if (codeDifference !== 0) {
    return codeDifference;
  }

  return left.id - right.id;
}

function sortProducts(
  products: readonly ProductItem[],
  sort: ProductSortParam,
): ProductItem[] {
  if (sort !== POSITIVE_COST_SORT) {
    return [...products];
  }

  return [...products].sort(compareProductsForPositiveCostSort);
}

function resolveVisibleProducts(
  products: readonly ProductItem[],
  query: ProductQueryState,
  utilityQuery: ProductUtilityQuery | null,
): ProductItem[] {
  return sortProducts(
    applyUtilityQuery(
      applyCategoryFilter(products, query.category, query.excludedCategories),
      utilityQuery,
    ),
    query.sort,
  );
}

function applyUtilityQuery(
  products: readonly ProductItem[],
  utilityQuery: ProductUtilityQuery | null,
): ProductItem[] {
  if (!utilityQuery) {
    return [...products];
  }

  return products.filter((product) => utilityQuery.matches(product));
}

function applyCategoryFilter(
  products: readonly ProductItem[],
  selectedCategories: readonly string[],
  excludedCategories: readonly string[],
): ProductItem[] {
  if (selectedCategories.length === 0 && excludedCategories.length === 0) {
    return [...products];
  }

  const normalizedCategories = new Set(
    selectedCategories.map((category) => category.trim().toLowerCase()),
  );
  const normalizedExcludedCategories = new Set(
    excludedCategories.map((category) => category.trim().toLowerCase()),
  );

  return products.filter((product) => {
    const normalizedProductCategory = product.category.trim().toLowerCase();
    const isIncluded =
      normalizedCategories.size === 0 ||
      normalizedCategories.has(normalizedProductCategory);
    const isExcluded = normalizedExcludedCategories.has(
      normalizedProductCategory,
    );

    return isIncluded && !isExcluded;
  });
}

function normalizeFormValuesForUtilityQuery(
  formValues: ProductFilterFormValues,
  previousUtilityQuery: ProductUtilityQuery | null,
  nextUtilityQuery: ProductUtilityQuery | null,
  categories: readonly ProductCategory[],
): ProductFilterFormValues {
  const previousExcludedCategories = getUtilityQueryExcludedCategories(
    previousUtilityQuery,
    categories,
  );
  const nextExcludedCategories = getUtilityQueryExcludedCategories(
    nextUtilityQuery,
    categories,
  );
  const nextFormValues = normalizeFormCategorySelections(
    {
      ...formValues,
      category: removeCategories(formValues.category, nextExcludedCategories),
      excludedCategories: mergeCategories(
        removeCategories(
          formValues.excludedCategories,
          previousExcludedCategories,
        ),
        nextExcludedCategories,
      ),
    },
    categories,
  );

  if (
    nextUtilityQuery?.id === "missing-barcode" &&
    nextFormValues.barcode.trim().length > 0
  ) {
    return {
      ...nextFormValues,
      barcode: "",
    };
  }

  return nextFormValues;
}

function buildCategoryOptions(
  categories: ProductCategory[],
  selectedCategories: readonly string[],
): SelectOption[] {
  const options: SelectOption[] = [];
  const normalizedCategoryNames = new Set(
    categories.map((category) => category.name.trim().toLowerCase()),
  );

  for (const selectedCategory of selectedCategories) {
    const normalizedSelectedCategory = selectedCategory.trim().toLowerCase();

    if (
      normalizedSelectedCategory &&
      !normalizedCategoryNames.has(normalizedSelectedCategory)
    ) {
      options.push({
        label: selectedCategory,
        value: selectedCategory,
      });
    }
  }

  for (const category of categories) {
    options.push({
      label: category.name,
      value: category.name,
    });
  }

  return options;
}

function buildReportTitle(
  query: ProductQueryState,
  utilityQuery: ProductUtilityQuery | null,
): string {
  const utilityQueryTitle = utilityQuery?.title.trim();

  if (utilityQueryTitle) {
    return utilityQueryTitle;
  }

  const textQuery = query.q.trim();

  if (textQuery) {
    return textQuery;
  }

  return DEFAULT_REPORT_TITLE;
}

function buildReportFilename(title: string): string {
  const normalizedTitle = title
    .trim()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/(^-+|-+$)/g, "");

  if (!normalizedTitle) {
    return "relatorio-produtos.pdf";
  }

  return `${normalizedTitle}.pdf`;
}

function createExportPayload(
  title: string,
  products: readonly ProductItem[],
): {
  title: string;
  products: Omit<ProductItem, "id">[];
} {
  return {
    title,
    products: products.map(({ id: _id, ...product }) => product),
  };
}

function downloadBlob(blob: Blob, filename: string) {
  const objectUrl = URL.createObjectURL(blob);
  const anchor = document.createElement("a");

  anchor.href = objectUrl;
  anchor.download = filename;
  document.body.appendChild(anchor);
  anchor.click();
  anchor.remove();

  window.setTimeout(() => {
    URL.revokeObjectURL(objectUrl);
  }, 0);
}

export function Product() {
  const [page, setPage] = useState(1);
  const [query, setQuery] = useState<ProductQueryState>(DEFAULT_QUERY_STATE);
  const [selectedUtilityQueryId, setSelectedUtilityQueryId] =
    useState<ProductUtilityQueryId | null>(null);
  const [formValues, setFormValues] = useState<ProductFilterFormValues>(
    toFormValues(DEFAULT_QUERY_STATE),
  );
  const [productsPage, setProductsPage] =
    useState<ApiPage<ProductListItem> | null>(null);
  const [categories, setCategories] = useState<ProductCategory[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCategoriesLoading, setIsCategoriesLoading] = useState(true);
  const [isExporting, setIsExporting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [exportErrorMessage, setExportErrorMessage] = useState<string | null>(
    null,
  );
  const [categoryErrorMessage, setCategoryErrorMessage] = useState<
    string | null
  >(null);

  const resolvedFormValues = normalizeFormCategorySelections(formValues, categories);
  const categoryOptions = buildCategoryOptions(
    categories,
    normalizeCategories([
      ...resolvedFormValues.category,
      ...resolvedFormValues.excludedCategories,
    ]),
  );
  const activeUtilityQuery = getProductUtilityQuery(selectedUtilityQueryId);
  const isBarcodeFilterDisabled = activeUtilityQuery?.id === "missing-barcode";
  const shouldUsePositiveCostSort = query.sort === POSITIVE_COST_SORT;
  const shouldUseClientSidePage =
    shouldUsePositiveCostSort ||
    activeUtilityQuery?.kind === "local-all" ||
    query.category.length > 1 ||
    query.excludedCategories.length > 0;

  useEffect(() => {
    const controller = new AbortController();

    void api.categories
      .findAll({
        signal: controller.signal,
      })
      .then((response) => {
        setCategories(response);
      })
      .catch((error: unknown) => {
        if (error instanceof DOMException && error.name === "AbortError") {
          return;
        }

        setCategoryErrorMessage(
          getErrorMessage(
            error,
            "Nao foi possivel carregar as categorias no momento.",
          ),
        );
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsCategoriesLoading(false);
        }
      });

    return () => controller.abort();
  }, []);

  useEffect(() => {
    if (!activeUtilityQuery || categories.length === 0) {
      return;
    }

    const nextFormValues = normalizeFormValuesForUtilityQuery(
      formValues,
      activeUtilityQuery,
      activeUtilityQuery,
      categories,
    );

    if (
      isSameQueryState(normalizeQuery(formValues), normalizeQuery(nextFormValues))
    ) {
      return;
    }

    setFormValues(nextFormValues);
  }, [activeUtilityQuery, categories, formValues]);

  useEffect(() => {
    const nextQuery = normalizeQuery(formValues);

    if (isSameQueryState(query, nextQuery)) {
      return;
    }

    const timeoutId = window.setTimeout(() => {
      setErrorMessage(null);
      setExportErrorMessage(null);
      setIsLoading(true);

      startTransition(() => {
        setPage(1);
        setQuery(nextQuery);
      });
    }, FILTER_AUTO_APPLY_DELAY_MS);

    return () => window.clearTimeout(timeoutId);
  }, [formValues, query]);

  useEffect(() => {
    const controller = new AbortController();
    const inventoryRequest = buildInventoryRequest(query);

    if (shouldUseClientSidePage) {
      void Promise.all([
        api.products.findAll(buildFindAllRequest(query), {
          signal: controller.signal,
        }),
        api.inventory.findAll(inventoryRequest, {
          signal: controller.signal,
        }),
      ])
        .then(([productsResponse, inventoryResponse]) => {
          const filteredProducts = resolveVisibleProducts(
            productsResponse,
            query,
            activeUtilityQuery,
          );

          setProductsPage(
            createClientPage(
              attachInventoryToProducts(filteredProducts, inventoryResponse),
              page,
              query.size,
            ),
          );
        })
        .catch((error: unknown) => {
          if (error instanceof DOMException && error.name === "AbortError") {
            return;
          }

          setErrorMessage(
            getErrorMessage(
              error,
              "Nao foi possivel carregar os produtos no momento.",
            ),
          );
        })
        .finally(() => {
          if (!controller.signal.aborted) {
            setIsLoading(false);
          }
        });
    } else {
      void Promise.all([
        api.products.findPaged(buildPagedRequest(query, page), {
          signal: controller.signal,
        }),
        api.inventory.findAll(inventoryRequest, {
          signal: controller.signal,
        }),
      ])
        .then(([productsResponse, inventoryResponse]) => {
          setProductsPage(
            attachInventoryToProductPage(productsResponse, inventoryResponse),
          );
        })
        .catch((error: unknown) => {
          if (error instanceof DOMException && error.name === "AbortError") {
            return;
          }

          setErrorMessage(
            getErrorMessage(
              error,
              "Nao foi possivel carregar os produtos no momento.",
            ),
          );
        })
        .finally(() => {
          if (!controller.signal.aborted) {
            setIsLoading(false);
          }
        });
    }

    return () => controller.abort();
  }, [activeUtilityQuery, page, query, shouldUseClientSidePage]);

  const currentPage = productsPage ? productsPage.number + 1 : page;
  const totalPages = productsPage?.totalPages ?? 0;
  const totalElements = productsPage?.totalElements ?? 0;
  const rangeStart =
    totalElements > 0 && productsPage
      ? productsPage.number * productsPage.size + 1
      : 0;
  const rangeEnd =
    totalElements > 0 && productsPage
      ? rangeStart + productsPage.numberOfElements - 1
      : 0;

  function runRequest(update: () => void) {
    setErrorMessage(null);
    setExportErrorMessage(null);
    setIsLoading(true);

    startTransition(() => {
      update();
    });
  }

  function handleFormValueChange(
    field: keyof ProductFilterFormValues,
    value: string | string[],
  ) {
    setFormValues((currentValues) => {
      if (field === "category") {
        const nextCategory = normalizeAndExpandCategories(
          Array.isArray(value) ? value : value ? [value] : [],
          categories,
        );

        return {
          ...currentValues,
          category: nextCategory,
          excludedCategories: removeCategories(
            currentValues.excludedCategories,
            nextCategory,
          ),
        };
      }

      if (field === "excludedCategories") {
        const nextExcludedCategories = normalizeAndExpandCategories(
          Array.isArray(value) ? value : value ? [value] : [],
          categories,
        );

        return {
          ...currentValues,
          category: removeCategories(
            currentValues.category,
            nextExcludedCategories,
          ),
          excludedCategories: nextExcludedCategories,
        };
      }

      return {
        ...currentValues,
        [field]: Array.isArray(value) ? value.join(",") : value,
      };
    });
  }

  function handleResetFilters() {
    const resetValues = toFormValues(DEFAULT_QUERY_STATE);

    setSelectedUtilityQueryId(null);
    setFormValues(resetValues);

    runRequest(() => {
      setPage(1);
      setQuery(DEFAULT_QUERY_STATE);
    });
  }

  function handlePageChange(nextPage: number) {
    if (nextPage === currentPage) {
      return;
    }

    runRequest(() => {
      setPage(nextPage);
    });
  }

  function handleUtilityQueryChange(nextQueryId: ProductUtilityQueryId | null) {
    const nextUtilityQuery = getProductUtilityQuery(nextQueryId);
    const nextFormValues = normalizeFormValuesForUtilityQuery(
      formValues,
      activeUtilityQuery,
      nextUtilityQuery,
      categories,
    );
    const nextQuery = normalizeQuery(nextFormValues);

    setSelectedUtilityQueryId(nextQueryId);
    setFormValues(nextFormValues);

    runRequest(() => {
      setPage(1);
      setQuery(nextQuery);
    });
  }

  async function handleExportReports() {
    setExportErrorMessage(null);
    setIsExporting(true);

    try {
      const reportTitle = buildReportTitle(query, activeUtilityQuery);
      const allProducts = await api.products.findAll(buildFindAllRequest(query));
      const visibleProducts = resolveVisibleProducts(
        allProducts,
        query,
        activeUtilityQuery,
      );
      const report = await api.products.exportPdf(
        createExportPayload(reportTitle, visibleProducts),
      );

      downloadBlob(report, buildReportFilename(reportTitle));
    } catch (error: unknown) {
      setExportErrorMessage(
        getErrorMessage(
          error,
          "Nao foi possivel exportar os relatorios no momento.",
        ),
      );
    } finally {
      setIsExporting(false);
    }
  }

  const emptyStateMessage = activeUtilityQuery
    ? `Nenhum produto encontrado para a consulta "${activeUtilityQuery.title}".`
    : "Nenhum produto encontrado para os filtros informados.";

  return (
    <section
      id="products"
      className="mx-auto flex min-h-screen w-full max-w-6xl items-start px-4 py-8 sm:px-6 lg:px-8"
    >
      <div className="w-full rounded-[2rem] border border-[color:var(--border-color)] bg-[color:var(--surface-color)] p-6 shadow-[0_24px_80px_var(--bg-accent-color)] backdrop-blur md:p-8">
        <div className="flex flex-col gap-3 border-b border-[color:var(--border-color)] pb-6">
          <div className="flex flex-col gap-2 md:flex-row md:items-end md:justify-between">
            <div className="space-y-2">
              <h1 className="text-3xl font-semibold text-[color:var(--text-color)] sm:text-4xl">
                Produtos
              </h1>
              <p className="max-w-2xl text-sm text-[color:var(--text-secondary-color)] sm:text-base">
                Busca, ordenação e filtros.
              </p>
            </div>

            {productsPage ? (
              <div className="rounded-2xl border border-[color:var(--border-color)] px-4 py-3 text-sm text-[color:var(--text-secondary-color)]">
                {rangeStart > 0 ? `${rangeStart}-${rangeEnd}` : "0"} de{" "}
                <strong className="font-semibold text-[color:var(--text-color)]">
                  {totalElements}
                </strong>{" "}
                itens
              </div>
            ) : null}
          </div>
        </div>
        <ProductUtilityQueries
          selectedQueryId={selectedUtilityQueryId}
          disabled={isLoading && !productsPage}
          onSelect={handleUtilityQueryChange}
        />

        <ProductFilters
          values={resolvedFormValues}
          categoryOptions={categoryOptions}
          isCategoryLoading={isCategoriesLoading}
          barcodeDisabled={isBarcodeFilterDisabled}
          onValueChange={handleFormValueChange}
          onReset={handleResetFilters}
          disabled={isLoading && !productsPage}
        />

        {categoryErrorMessage ? (
          <div className="mt-4 rounded-3xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm text-amber-800">
            {categoryErrorMessage}
          </div>
        ) : null}

        {isLoading && !productsPage ? (
          <div className="flex min-h-72 items-center justify-center text-base text-[color:var(--text-secondary-color)]">
            Carregando produtos...
          </div>
        ) : null}

        {errorMessage ? (
          <div className="mt-6 rounded-3xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700">
            {errorMessage}
          </div>
        ) : null}

        {!errorMessage && productsPage && productsPage.content.length === 0 ? (
          <div className="mt-6 rounded-3xl border border-dashed border-[color:var(--border-color)] px-5 py-12 text-center text-[color:var(--text-secondary-color)]">
            {emptyStateMessage}
          </div>
        ) : null}

        {!errorMessage && productsPage && productsPage.content.length > 0 ? (
          <div className="mt-6 space-y-4">
            <div className="flex flex-col gap-4 border-b border-[color:var(--border-color)] pb-6 md:grid md:grid-cols-[1fr_auto_1fr] md:items-center">
              <p className="text-sm text-[color:var(--text-secondary-color)] md:justify-self-start">
                Pagina {currentPage} de {Math.max(totalPages, 1)}
              </p>

              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
                disabled={isLoading}
                siblingCount={PRODUCT_PAGINATION_SIBLING_COUNT}
              />

              <div className="hidden md:block" aria-hidden="true" />
            </div>

            <div className="flex justify-end">
              <button
                type="button"
                onClick={() => void handleExportReports()}
                disabled={isLoading || isExporting}
                className={PRODUCT_SECONDARY_ACTION_BUTTON_CLASSES}
              >
                {isExporting ? "Exportando relatorios..." : "Exportar relatorios"}
              </button>
            </div>

            {exportErrorMessage ? (
              <div className="rounded-3xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm text-rose-700">
                {exportErrorMessage}
              </div>
            ) : null}

            <ProductList
              products={productsPage.content}
              isLoading={isLoading}
            />
          </div>
        ) : null}
      </div>
    </section>
  );
}
