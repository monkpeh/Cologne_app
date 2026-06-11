export interface Fragrance {
  id: number;
  brand: string;
  name: string;
  scentFamily: string;
  projection: string;
  longevity: string;
  seasonHot: boolean;
  seasonCold: boolean;
  officeSafe: boolean;
  description: string;
  imageUrl?: string;
}

export interface AppUser {
  id: number;
  username: string;
  role: string;
}

export interface AuthResponse {
  token: string;
  username: string;
  role: string;
}

export interface CollectionEntry {
  fragrance: Fragrance;
  rating?: number;
}

export interface UserStats {
  totalFragrances: number;
  averageRating: number;
  scentFamilyBreakdown: Record<string, number>;
  topRated: Fragrance[];
  highestProjection?: Fragrance;
  longestLasting?: Fragrance;
}

export interface RecommendationResult {
  fragrance: Fragrance;
  score: number;
  reasons: string[];
}

export interface MetaOption {
  value: string;
  label: string;
}

export interface BrowseResponse {
  fragrances: Fragrance[];
  collectionIds: number[];
  averageRatings: Record<number, number>;
}
