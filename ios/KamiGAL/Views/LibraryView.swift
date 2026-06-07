import SwiftUI

struct LibraryView: View {
    @EnvironmentObject var gameStore: GameStore
    @State private var searchText = ""
    @State private var showFavoritesOnly = false
    @State private var selectedCategory: String?
    @State private var selectedGame: Game?

    var displayedGames: [Game] {
        let source = showFavoritesOnly ? gameStore.favoriteGames : gameStore.games
        let categoryFiltered = selectedCategory != nil ? source.filter { $0.categories?.contains(selectedCategory!) ?? false } : source
        if searchText.isEmpty { return categoryFiltered }
        return categoryFiltered.filter {
            $0.title.localizedCaseInsensitiveContains(searchText) ||
            ($0.developer?.localizedCaseInsensitiveContains(searchText) ?? false)
        }
    }

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 搜索栏
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("搜索游戏...", text: $searchText)
                        .foregroundColor(.white)
                }
                .padding(10)
                .background(Color(.systemGray6).opacity(0.2))
                .cornerRadius(10)
                .padding(.horizontal)
                .padding(.top, 8)

                // 筛选栏
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        FilterChip(text: "全部", isSelected: !showFavoritesOnly && selectedCategory == nil) {
                            showFavoritesOnly = false
                            selectedCategory = nil
                        }
                        FilterChip(text: "⭐ 收藏", isSelected: showFavoritesOnly) {
                            showFavoritesOnly.toggle()
                            selectedCategory = nil
                        }
                        ForEach(gameStore.allCategories, id: \.self) { cat in
                            FilterChip(text: cat, isSelected: selectedCategory == cat) {
                                selectedCategory = cat
                                showFavoritesOnly = false
                            }
                        }
                    }
                    .padding(.horizontal)
                    .padding(.vertical, 8)
                }

                // 游戏列表
                ScrollView {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
                        ForEach(displayedGames) { game in
                            GameCardView(game: game)
                                .onTapGesture { selectedGame = game }
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle("游戏库")
            .background(Color.black.edgesIgnoringSafeArea(.all))
            .sheet(item: $selectedGame) { game in
                GameDetailView(game: game)
            }
        }
    }
}

struct FilterChip: View {
    let text: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Text(text)
            .font(.system(size: 13))
            .foregroundColor(isSelected ? .white : .gray)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isSelected ? Color(red: 0.6, green: 0.4, blue: 0.9) : Color(.systemGray6).opacity(0.2))
            .cornerRadius(16)
            .onTapGesture(perform: action)
    }
}

struct GameCardView: View {
    @EnvironmentObject var gameStore: GameStore
    let game: Game

    var body: some View {
        ZStack(alignment: .topTrailing) {
            VStack(alignment: .leading, spacing: 6) {
                // 封面占位
                Rectangle()
                    .fill(Color(.systemGray6).opacity(0.3))
                    .aspectRatio(3/4, contentMode: .fill)
                    .overlay(
                        Image(systemName: "photo")
                            .font(.largeTitle)
                            .foregroundColor(.gray)
                    )
                    .cornerRadius(8)

                // 标题
                Text(game.title)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(.white)
                    .lineLimit(2)

                // 开发商
                if let dev = game.developer {
                    Text(dev)
                        .font(.system(size: 11))
                        .foregroundColor(.gray)
                        .lineLimit(1)
                }

                // 状态标签
                HStack(spacing: 4) {
                    Text(game.playStatus.displayName)
                        .font(.system(size: 10))
                        .foregroundColor(.gray)
                        .padding(.horizontal, 6)
                        .padding(.vertical, 2)
                        .background(Color(.systemGray6).opacity(0.3))
                        .cornerRadius(4)

                    if game.favorite {
                        Image(systemName: "star.fill")
                            .font(.system(size: 10))
                            .foregroundColor(.yellow)
                    }
                }
            }

            // 收藏星标
            if game.favorite {
                Image(systemName: "star.fill")
                    .font(.system(size: 16))
                    .foregroundColor(.yellow)
                    .shadow(color: .black.opacity(0.5), radius: 2)
                    .padding(6)
            }
        }
        .padding(8)
        .background(Color(.systemGray6).opacity(0.1))
        .cornerRadius(12)
    }
}