import { ProductCard } from "./ProductCard";
import type { ProductListItem } from "./product.types";

export interface ProductListProps {
  products: readonly ProductListItem[];
  isLoading?: boolean;
}

export function ProductList({
  products,
  isLoading = false,
}: ProductListProps) {
  return (
    <ul className={isLoading ? "space-y-4 opacity-70" : "space-y-4"}>
      {products.map((product) => (
        <ProductCard key={product.id} product={product} />
      ))}
    </ul>
  );
}
