'use client';

import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getCollection, removeFromCollection, rateFragrance } from '@/lib/api/fragrance';
import { Fragrance } from '@/lib/api/types';

export default function CollectionPage() {
    const queryClient = useQueryClient();

    const { data: collection = [], isLoading } = useQuery({
        queryKey: ['collection'],
        queryFn: getCollection,
    });

    const removeMutation = useMutation({
        mutationFn: removeFromCollection,
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['collection'] }),
    });

    const rateMutation = useMutation({
        mutationFn: ({ id, rating }: { id: number; rating: number }) => rateFragrance(id, rating),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['collection'] }),
    });

    if (isLoading) {
        return <p className="text-neutral-400">Loading collection...</p>;
    }

    if (collection.length === 0) {
        return (
            <div>
                <h1 className="text-2xl font-bold text-white mb-2">My Collection</h1>
                <p className="text-neutral-400">
                    Your collection is empty.{' '}
                    <a href="/browse" className="text-amber-400 hover:underline">Browse fragrances</a> to add some.
                </p>
            </div>
        );
    }

    return (
        <div>
            <h1 className="text-2xl font-bold text-white mb-6">My Collection</h1>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                {collection.map((fragrance: Fragrance) => (
                    <div key={fragrance.id} className="bg-neutral-900 border border-neutral-800 rounded-xl p-4">
                        {fragrance.imageUrl && (
                            <img src={fragrance.imageUrl} alt={fragrance.name} className="w-full h-32 object-contain mb-3" />
                        )}
                        <p className="text-amber-400 text-xs font-medium uppercase tracking-wide">{fragrance.brand}</p>
                        <h3 className="text-white font-semibold">{fragrance.name}</h3>
                        <p className="text-neutral-500 text-sm">{fragrance.scentFamily}</p>

                        {/* Star rating */}
                        <div className="flex gap-1 my-3">
                            {[1, 2, 3, 4, 5].map((star) => (
                                <button
                                    key={star}
                                    onClick={() => rateMutation.mutate({ id: fragrance.id, rating: star })}
                                    className="text-lg text-neutral-600 hover:text-amber-400 transition-colors"
                                >
                                    ★
                                </button>
                            ))}
                        </div>

                        <button
                            onClick={() => removeMutation.mutate(fragrance.id)}
                            className="text-red-400 hover:text-red-300 text-sm transition-colors"
                        >
                            Remove
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}