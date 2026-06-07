import Foundation
import SQLite3

class GameStore: ObservableObject {
    @Published var games: [Game] = []
    @Published var favoriteGames: [Game] = []
    @Published var searchResults: [Game] = []
    @Published var isLoading = false
    @Published var selectedCategory: String?

    private var db: OpaquePointer?
    private let dbName = "kamigal.db"

    init() {
        openDatabase()
        createTable()
        loadGames()
    }

    deinit {
        if db != nil {
            sqlite3_close(db)
        }
    }

    private func openDatabase() {
        let paths = NSSearchPathForDirectoriesInDomains(.documentDirectory, .userDomainMask, true)
        let documentsPath = paths[0]
        let dbPath = "\(documentsPath)/\(dbName)"

        if sqlite3_open(dbPath, &db) != SQLITE_OK {
            print("数据库打开失败")
        }
    }

    private func createTable() {
        let createSQL = """
        CREATE TABLE IF NOT EXISTS games (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            title TEXT NOT NULL,
            titleLocal TEXT,
            developer TEXT,
            releaseDate TEXT,
            coverUri TEXT,
            descriptionText TEXT,
            rating REAL DEFAULT 0,
            vndbId TEXT,
            tags TEXT,
            favorite INTEGER DEFAULT 0,
            playStatus TEXT DEFAULT 'unplayed',
            totalPlayTime INTEGER DEFAULT 0,
            lastPlayedAt INTEGER,
            screenshotUris TEXT,
            engineType TEXT DEFAULT 'other',
            categories TEXT,
            createdAt INTEGER DEFAULT (strftime('%s','now'))
        );
        """
        var statement: OpaquePointer?
        if sqlite3_exec(db, createSQL, nil, nil, nil) != SQLITE_OK {
            print("建表失败: \(String(cString: sqlite3_errmsg(db)))")
        }
        sqlite3_finalize(statement)
    }

    func loadGames() {
        var statement: OpaquePointer?
        let query = "SELECT * FROM games ORDER BY favorite DESC, title ASC"

        var loadedGames: [Game] = []
        if sqlite3_prepare_v2(db, query, -1, &statement, nil) == SQLITE_OK {
            while sqlite3_step(statement) == SQLITE_ROW {
                let game = parseGame(statement: statement)
                loadedGames.append(game)
            }
        }
        sqlite3_finalize(statement)

        DispatchQueue.main.async {
            self.games = loadedGames
            self.favoriteGames = loadedGames.filter { $0.favorite }
        }
    }

    private func parseGame(statement: OpaquePointer?) -> Game {
        let id = sqlite3_column_int64(statement, 0)
        let title = String(cString: sqlite3_column_text(statement, 1))
        let titleLocal = sqlite3_column_text(statement, 2).map { String(cString: $0) }
        let developer = sqlite3_column_text(statement, 3).map { String(cString: $0) }
        let releaseDate = sqlite3_column_text(statement, 4).map { String(cString: $0) }
        let coverUri = sqlite3_column_text(statement, 5).map { String(cString: $0) }
        let descriptionText = sqlite3_column_text(statement, 6).map { String(cString: $0) }
        let rating = sqlite3_column_double(statement, 7)
        let vndbId = sqlite3_column_text(statement, 8).map { String(cString: $0) }
        let tagsStr = sqlite3_column_text(statement, 9).map { String(cString: $0) }
        let favorite = sqlite3_column_int(statement, 10) == 1
        let playStatusStr = String(cString: sqlite3_column_text(statement, 11))
        let totalPlayTime = sqlite3_column_int64(statement, 12)
        let lastPlayedAt = sqlite3_column_int64(statement, 13)
        let screenshotStr = sqlite3_column_text(statement, 14).map { String(cString: $0) }
        let categoriesStr = sqlite3_column_text(statement, 16).map { String(cString: $0) }

        var tags: [String]? = nil
        if let t = tagsStr, !t.isEmpty {
            tags = t.split(separator: ",").map(String.init)
        }

        var screenshots: [String]? = nil
        if let s = screenshotStr, !s.isEmpty {
            screenshots = s.split(separator: ",").map(String.init)
        }

        var categories: [String]? = nil
        if let c = categoriesStr, !c.isEmpty {
            categories = c.split(separator: ",").map(String.init)
        }

        return Game(
            id: id,
            title: title,
            titleLocal: titleLocal,
            developer: developer,
            releaseDate: releaseDate,
            coverUri: coverUri,
            descriptionText: descriptionText,
            rating: rating,
            vndbId: vndbId,
            tags: tags,
            favorite: favorite,
            playStatus: Game.PlayStatus(rawValue: playStatusStr) ?? .unplayed,
            totalPlayTime: totalPlayTime,
            lastPlayedAt: lastPlayedAt > 0 ? Date(timeIntervalSince1970: TimeInterval(lastPlayedAt)) : nil,
            screenshotUris: screenshots,
            categories: categories
        )
    }

    func toggleFavorite(_ game: Game) {
        let newVal = game.favorite ? 0 : 1
        var statement: OpaquePointer?
        let update = "UPDATE games SET favorite = ? WHERE id = ?"
        if sqlite3_prepare_v2(db, update, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_int(statement, 1, Int32(newVal))
            sqlite3_bind_int64(statement, 2, game.id)
            sqlite3_step(statement)
        }
        sqlite3_finalize(statement)
        loadGames()
    }

    func searchGames(query: String) {
        guard !query.isEmpty else {
            searchResults = []
            return
        }
        var statement: OpaquePointer?
        let search = "SELECT * FROM games WHERE title LIKE ? OR developer LIKE ? OR vndbId LIKE ? ORDER BY favorite DESC"
        let pattern = "%\(query)%"

        var results: [Game] = []
        if sqlite3_prepare_v2(db, search, -1, &statement, nil) == SQLITE_OK {
            sqlite3_bind_text(statement, 1, (pattern as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 2, (pattern as NSString).utf8String, -1, nil)
            sqlite3_bind_text(statement, 3, (pattern as NSString).utf8String, -1, nil)

            while sqlite3_step(statement) == SQLITE_ROW {
                results.append(parseGame(statement: statement))
            }
        }
        sqlite3_finalize(statement)

        DispatchQueue.main.async {
            self.searchResults = results
        }
    }

    func gamesByCategory(_ category: String?) -> [Game] {
        guard let cat = category else { return games }
        return games.filter { $0.categories?.contains(cat) ?? false }
    }

    var allCategories: [String] {
        var cats = Set<String>()
        for game in games {
            if let c = game.categories {
                for cat in c { cats.insert(cat) }
            }
        }
        return Array(cats).sorted()
    }

    var allTags: [String] {
        var tagSet = Set<String>()
        for game in games {
            if let t = game.tags {
                for tag in t { tagSet.insert(tag) }
            }
        }
        return Array(tagSet).sorted()
    }
}