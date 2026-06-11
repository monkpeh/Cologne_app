//AppUser
export interface AppUser {
    id: number;
    username: string;
    role: 'USER' | 'ADMIN';
    collectionIds: number[];
    ratings: Record<number, number>;
}

//Fragrance
export interface Fragrance {
    id: number;
    brand: string;
    name: string;
    scentFamily: string;
    projection: number;
    longevity: number;
    seasonHot: number;
    seasonCold: number;
    officeSafe: boolean;
    description: string | null;
    imageUrl: string | null;
}

//UserStats
export interface UserStats {
    totalOwned: number;
    totalRated: number;
    averageRating: number;
    familyCounts: Record<string, number>;
    officeSafeCount: number;
    casualCount: number;
    topRated: Fragrance[];
    mostProjecting: Fragrance | null;
    longestLasting: Fragrance | null;
}

//ScoredFragrance
export interface ScoredFragrance {
    fragrance: Fragrance;
    score: number;
    reasons: string[];
    scorePercent: string;
}

//RecommendationResponse
export interface RecommendationResponse {
    correlationId: string;
    results: ScoredFragrance[];
}

//Weather and Occasion
export type Weather = 'HOT' | 'WARM' | 'MILD' | 'COOL' | 'COLD';
export type Occasion = 'CASUAL' | 'OFFICE' | 'DATE' | 'FORMAL' | 'SOCIAL';