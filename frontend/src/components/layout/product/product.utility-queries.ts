import type { Category as ProductCategory, Product } from "../../../api";

export type ProductUtilityQueryId =
  | "missing-barcode"
  | "low-profit-margin"
  | "negative-profit-margin";

interface CategoryExpansionRule {
  aliases: readonly string[];
  matchTerms: readonly string[];
}

interface BaseProductUtilityQuery {
  id: ProductUtilityQueryId;
  title: string;
  description: string;
  excludedCategories?: readonly string[];
}

export interface ProductLocalUtilityQuery extends BaseProductUtilityQuery {
  kind: "local-all";
  matches: (product: Product) => boolean;
}

export type ProductUtilityQuery = ProductLocalUtilityQuery;

const CATEGORY_EXPANSION_RULES: readonly CategoryExpansionRule[] = [
  {
    aliases: ["qualquer tipo de pneu"],
    matchTerms: ["pneu"],
  },
  {
    aliases: ["parafusos", "qualquer tipo de parafuso"],
    matchTerms: ["parafuso"],
  },
] as const;

const MISSING_BARCODE_EXCLUDED_CATEGORIES = [
  "combustiveis",
  "chaves",
  "parafusos",
  "arruelas",
  "porcas",
  "abracadeiras",
  "botinas rogil",
  "carnes",
  "galoes vazios",
  "glp/vasilhames",
  "lubrificantesgranel",
  "mangueiras/bicos",
  "outroscadastros",
  "qualquer tipo de parafuso",
  "pecas paramoto",
  "pinos/cupilhas/barraderosca",
  "qualquer tipo de pneu",
  "protetores",
  "sapatos rogil",
  "sinuca",
  "servicos",
  "terminal/engate/bujaocarter",
] as const;

const LOW_PROFIT_MARGIN_THRESHOLD = 20;

const LOW_PROFIT_MARGIN_EXCLUDED_CATEGORIES = ["combustiveis"] as const;

function hasBarcode(product: Product): boolean {
  return (product.barcode ?? "").trim().length > 0;
}

function hasProfitMarginBelowThreshold(
  product: Product,
  threshold: number,
): boolean {
  return Number.isFinite(product.profitMargin) && product.profitMargin < threshold;
}

function hasNegativeProfitMargin(product: Product): boolean {
  return Number.isFinite(product.profitMargin) && product.profitMargin < 0;
}

function normalizeCategoryValue(value: string): string {
  return value
    .trim()
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/\\/g, "/")
    .replace(/\s*\/\s*/g, "/")
    .replace(/\s+/g, "");
}

function compareCategoryNames(left: string, right: string): number {
  return left.localeCompare(right);
}

function getUniqueCategoryNames(values: readonly string[]): string[] {
  const seenCategoryNames = new Set<string>();
  const uniqueValues: string[] = [];

  for (const value of values) {
    const normalizedValue = normalizeCategoryValue(value);

    if (!normalizedValue || seenCategoryNames.has(normalizedValue)) {
      continue;
    }

    seenCategoryNames.add(normalizedValue);
    uniqueValues.push(value);
  }

  return uniqueValues;
}

function findCategoryExpansionRule(
  categoryName: string,
): CategoryExpansionRule | null {
  const normalizedCategoryName = normalizeCategoryValue(categoryName);

  return (
    CATEGORY_EXPANSION_RULES.find((rule) =>
      rule.aliases.some(
        (alias) => normalizeCategoryValue(alias) === normalizedCategoryName,
      ),
    ) ?? null
  );
}

function findExactCategoryMatches(
  categories: readonly ProductCategory[],
  value: string,
): string[] {
  const normalizedValue = normalizeCategoryValue(value);

  return categories
    .map((category) => category.name)
    .filter(
      (categoryName) => normalizeCategoryValue(categoryName) === normalizedValue,
    );
}

function findCategoriesMatchingTerms(
  categories: readonly ProductCategory[],
  terms: readonly string[],
): string[] {
  const normalizedTerms = terms.map(normalizeCategoryValue);

  return categories
    .map((category) => category.name)
    .filter((categoryName) => {
      const normalizedCategoryName = normalizeCategoryValue(categoryName);

      return normalizedTerms.some(
        (term) => term.length > 0 && normalizedCategoryName.includes(term),
      );
    });
}

function resolveCategoryValue(
  value: string,
  categories: readonly ProductCategory[],
): string[] {
  if (categories.length === 0) {
    return findCategoryExpansionRule(value) ? [] : [value];
  }

  const exactMatches = findExactCategoryMatches(categories, value);

  if (exactMatches.length > 0) {
    return exactMatches;
  }

  const expansionRule = findCategoryExpansionRule(value);

  if (!expansionRule) {
    return [value];
  }

  return findCategoriesMatchingTerms(categories, expansionRule.matchTerms);
}

export const PRODUCT_UTILITY_QUERIES = [
  {
    id: "missing-barcode",
    title: "Produtos sem codigo de barras",
    description: "Lista itens que ainda nao possuem codigo de barras cadastrado.",
    excludedCategories: MISSING_BARCODE_EXCLUDED_CATEGORIES,
    kind: "local-all",
    matches: (product) => !hasBarcode(product),
  },
  {
    id: "low-profit-margin",
    title: "Produtos com baixo lucro",
    description: "Lista itens com margem de lucro abaixo de 20%.",
    excludedCategories: LOW_PROFIT_MARGIN_EXCLUDED_CATEGORIES,
    kind: "local-all",
    matches: (product) =>
      hasProfitMarginBelowThreshold(product, LOW_PROFIT_MARGIN_THRESHOLD),
  },
  {
    id: "negative-profit-margin",
    title: "Produtos com margem negativa",
    description: "Lista itens com margem de lucro abaixo de 0%.",
    kind: "local-all",
    matches: (product) => hasNegativeProfitMargin(product),
  },
] satisfies readonly ProductUtilityQuery[];

export function getProductUtilityQuery(
  queryId: ProductUtilityQueryId | null | undefined,
): ProductUtilityQuery | null {
  if (!queryId) {
    return null;
  }

  return PRODUCT_UTILITY_QUERIES.find((query) => query.id === queryId) ?? null;
}

export function getUtilityQueryExcludedCategories(
  utilityQuery: ProductUtilityQuery | null,
  categories: readonly ProductCategory[],
): string[] {
  if (!utilityQuery?.excludedCategories?.length) {
    return [];
  }

  return expandCategorySelections(utilityQuery.excludedCategories, categories);
}

export function expandCategorySelections(
  values: readonly string[],
  categories: readonly ProductCategory[],
): string[] {
  if (values.length === 0) {
    return [];
  }

  return getUniqueCategoryNames(
    values.flatMap((value) => resolveCategoryValue(value, categories)),
  ).sort(compareCategoryNames);
}
