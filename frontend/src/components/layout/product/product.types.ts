import type { Product } from "../../../api";

export type ProductListItem = Product & {
  quantityInStock: number | null;
};
