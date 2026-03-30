export interface ApiSortState {
  empty: boolean;
  sorted: boolean;
  unsorted: boolean;
}

export interface ApiPageable {
  offset: number;
  pageNumber: number;
  pageSize: number;
  paged: boolean;
  sort: ApiSortState;
  unpaged: boolean;
}

export interface ApiPage<TItem> {
  content: TItem[];
  empty: boolean;
  first: boolean;
  last: boolean;
  number: number;
  numberOfElements: number;
  pageable: ApiPageable;
  size: number;
  sort: ApiSortState;
  totalElements: number;
  totalPages: number;
}
