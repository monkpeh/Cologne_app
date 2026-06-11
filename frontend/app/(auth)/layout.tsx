export default function AuthLayout({
                                       children,
                                   }: {
    children: React.ReactNode;
}) {
    return (
        <main className="min-h-screen flex items-center justify-center bg-neutral-950">
            <div className="w-full max-w-sm px-6">
                <div className="text-center mb-8">
                    <h1 className="text-3xl font-bold text-amber-400">Cologne Advisor</h1>
                    <p className="text-neutral-400 mt-1 text-sm">Your personal fragrance companion</p>
                </div>
                {children}
            </div>
        </main>
    );
}