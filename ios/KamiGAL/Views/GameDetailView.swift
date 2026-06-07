import SwiftUI

struct GameDetailView: View {
    @EnvironmentObject var gameStore: GameStore
    @State var game: Game
    @State private var screenshots: [String] = []
    @State private var isLoadingScreenshots = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 16) {
                    // 封面
                    Rectangle()
                        .fill(Color(.systemGray6).opacity(0.3))
                        .aspectRatio(16/9, contentMode: .fit)
                        .overlay(
                            Image(systemName: "photo")
                                .font(.system(size: 48))
                                .foregroundColor(.gray)
                        )
                        .cornerRadius(12)

                    // 标题区域
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Text(game.title)
                                .font(.title2)
                                .bold()
                                .foregroundColor(.white)

                            Spacer()

                            Button {
                                gameStore.toggleFavorite(game)
                                game.favorite.toggle()
                            } label: {
                                Image(systemName: game.favorite ? "star.fill" : "star")
                                    .font(.title3)
                                    .foregroundColor(game.favorite ? .yellow : .gray)
                            }
                        }

                        if let local = game.titleLocal {
                            Text(local)
                                .font(.subheadline)
                                .foregroundColor(.gray)
                        }

                        // 信息行
                        HStack(spacing: 16) {
                            if let dev = game.developer {
                                Label(dev, systemImage: "building")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                            if let date = game.releaseDate {
                                Label(date, systemImage: "calendar")
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                            if let rating = game.rating, rating > 0 {
                                Label(String(format: "%.1f", rating), systemImage: "star.fill")
                                    .font(.caption)
                                    .foregroundColor(.yellow)
                            }
                        }
                    }
                    .padding(.horizontal)

                    // 状态
                    HStack(spacing: 12) {
                        ForEach(Game.PlayStatus.allCases, id: \.self) { status in
                            Text(status.displayName)
                                .font(.caption)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(game.playStatus == status ? Color(red: 0.6, green: 0.4, blue: 0.9) : Color(.systemGray6).opacity(0.2))
                                .cornerRadius(8)
                                .foregroundColor(game.playStatus == status ? .white : .gray)
                                .onTapGesture { game.playStatus = status }
                        }
                    }
                    .padding(.horizontal)

                    // 简介
                    if let desc = game.descriptionText {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("简介")
                                .font(.headline)
                                .foregroundColor(.white)
                            Text(desc)
                                .font(.body)
                                .foregroundColor(.gray)
                                .lineLimit(nil)
                        }
                        .padding(.horizontal)
                    }

                    // 标签
                    if let tags = game.tags, !tags.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("标签")
                                .font(.headline)
                                .foregroundColor(.white)
                            FlowLayout(spacing: 8) {
                                ForEach(tags, id: \.self) { tag in
                                    Text(tag)
                                        .font(.caption)
                                        .padding(.horizontal, 10)
                                        .padding(.vertical, 4)
                                        .background(Color(.systemGray6).opacity(0.2))
                                        .cornerRadius(6)
                                        .foregroundColor(.gray)
                                }
                            }
                        }
                        .padding(.horizontal)
                    }

                    // 截图
                    if !screenshots.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("截图")
                                .font(.headline)
                                .foregroundColor(.white)
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 8) {
                                    ForEach(screenshots, id: \.self) { url in
                                        AsyncImage(url: URL(string: url)) { phase in
                                            switch phase {
                                            case .success(let image):
                                                image.resizable().aspectRatio(contentMode: .fill)
                                                    .frame(width: 200, height: 120)
                                                    .cornerRadius(8)
                                            case .failure:
                                                Rectangle().fill(Color(.systemGray6))
                                                    .frame(width: 200, height: 120)
                                                    .cornerRadius(8)
                                            case .empty:
                                                ProgressView().frame(width: 200, height: 120)
                                            @unknown default:
                                                EmptyView()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
            .background(Color.black.edgesIgnoringSafeArea(.all))
            .navigationBarTitleDisplayMode(.inline)
            .task {
                if let vndbId = game.vndbId, !vndbId.isEmpty {
                    isLoadingScreenshots = true
                    if let urls = try? await VNDBService.shared.fetchScreenshots(vndbId: vndbId) {
                        screenshots = urls
                    }
                    isLoadingScreenshots = false
                }
            }
        }
    }
}

// 流式布局组件
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? 0
        var height: CGFloat = 0
        var x: CGFloat = 0
        var y: CGFloat = 0

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > width {
                y += size.height + spacing
                x = 0
            }
            x += size.width + spacing
            height = max(height, y + size.height)
        }
        return CGSize(width: width, height: height)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY

        for view in subviews {
            let size = view.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX {
                y += size.height + spacing
                x = bounds.minX
            }
            view.place(at: CGPoint(x: x, y: y), proposal: .unspecified)
            x += size.width + spacing
        }
    }
}

struct SearchView: View {
    @EnvironmentObject var gameStore: GameStore
    @State private var searchQuery = ""
    @State private var vndbResults: [VNDBResult] = []
    @State private var isSearching = false
    @State private var searchError: String?

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                // 搜索栏
                HStack {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.gray)
                    TextField("搜索VNDB...", text: $searchQuery)
                        .foregroundColor(.white)
                        .onSubmit { searchVNDB() }

                    if !searchQuery.isEmpty {
                        Button { searchQuery = "" } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundColor(.gray)
                        }
                    }

                    Button("搜索") { searchVNDB() }
                        .foregroundColor(Color(red: 0.6, green: 0.4, blue: 0.9))
                }
                .padding(10)
                .background(Color(.systemGray6).opacity(0.2))
                .cornerRadius(10)
                .padding()

                if isSearching {
                    Spacer()
                    ProgressView("搜索中...")
                    Spacer()
                } else if let error = searchError {
                    Spacer()
                    Text(error)
                        .foregroundColor(.red)
                    Spacer()
                } else if !vndbResults.isEmpty {
                    List(vndbResults) { result in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(result.title ?? "未知")
                                .font(.headline)
                                .foregroundColor(.white)
                            if let dev = result.developers?.first?.name {
                                Text(dev)
                                    .font(.caption)
                                    .foregroundColor(.gray)
                            }
                            HStack {
                                if let released = result.released {
                                    Text(released)
                                        .font(.caption)
                                        .foregroundColor(.gray)
                                }
                                Text("评分: \(result.displayRating)")
                                    .font(.caption)
                                    .foregroundColor(.yellow)
                            }
                        }
                        .padding(.vertical, 4)
                        .listRowBackground(Color.black)
                    }
                    .listStyle(.plain)
                } else {
                    Spacer()
                    Text("输入关键词搜索VNDB上的Galgame")
                        .foregroundColor(.gray)
                    Spacer()
                }
            }
            .navigationTitle("搜索")
            .background(Color.black.edgesIgnoringSafeArea(.all))
        }
    }

    private func searchVNDB() {
        guard !searchQuery.isEmpty else { return }
        isSearching = true
        searchError = nil

        Task {
            do {
                let results = try await VNDBService.shared.searchGame(query: searchQuery)
                await MainActor.run {
                    vndbResults = results
                    isSearching = false
                }
            } catch {
                await MainActor.run {
                    searchError = "搜索失败: \(error.localizedDescription)"
                    isSearching = false
                }
            }
        }
    }
}