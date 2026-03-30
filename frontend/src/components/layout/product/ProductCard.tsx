import type { ProductListItem } from "./product.types";

const currencyFormatter = new Intl.NumberFormat("pt-BR", {
  style: "currency",
  currency: "BRL",
});

const percentFormatter = new Intl.NumberFormat("pt-BR", {
  style: "percent",
  minimumFractionDigits: 2,
});

const quantityFormatter = new Intl.NumberFormat("pt-BR", {
  maximumFractionDigits: 3,
});

export interface ProductCardProps {
  product: ProductListItem;
}

export function ProductCard({ product }: ProductCardProps) {
  const quantityInStockLabel =
    product.quantityInStock != null
      ? quantityFormatter.format(product.quantityInStock)
      : "Nao informado";

  const profitMarginLabel =
    product.profitMargin != null
      ? percentFormatter.format(Number(product.profitMargin) / 100)
      : "Nao informado";

  const costLabel =
    product.cost != null
      ? currencyFormatter.format(product.cost)
      : "Nao informado";

  return (
    <li className="grid gap-5 rounded-[1.75rem] border border-[color:var(--border-color)] bg-[color:var(--bg-color)] p-5 shadow-[0_12px_36px_var(--bg-accent-color)] md:grid-cols-[minmax(0,2.25fr)_repeat(5,minmax(0,1fr))] md:items-center">
      <div className="space-y-2">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Produto
        </p>

        <div>
          <h2 className="text-xl font-semibold text-[color:var(--text-color)]">
            {product.name}
          </h2>
          <p className="mt-1 text-sm text-[color:var(--text-secondary-color)]">
            Codigo interno {product.code}
          </p>
        </div>
      </div>

      <div className="space-y-1">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Categoria
        </p>
        <p className="text-sm font-medium text-[color:var(--text-color)]">
          {product.category ?? "Nao informado"}
        </p>
      </div>

      <div className="space-y-1">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Quantidade em estoque
        </p>
        <p className="text-sm font-medium text-[color:var(--text-color)]">
          {quantityInStockLabel}
        </p>
      </div>

      <div className="space-y-1">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Custo
        </p>
        <p className="text-sm font-medium text-[color:var(--text-color)]">
          {costLabel}
        </p>
      </div>

      <div className="space-y-1">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Margem de lucro
        </p>
        <p className="text-sm font-medium text-[color:var(--text-color)]">
          {profitMarginLabel}
        </p>
      </div>

      <div className="space-y-1 md:text-right">
        <p className="text-xs font-semibold uppercase tracking-[0.28em] text-[color:var(--text-secondary-color)]">
          Preco
        </p>
        <p className="text-xl font-semibold text-[color:var(--text-color)]">
          {currencyFormatter.format(product.price)}
        </p>
      </div>
    </li>
  );
}
