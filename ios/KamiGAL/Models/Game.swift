import Foundation

struct Game: Codable, Identifiable, Hashable {
    let id: Int64
    var title: String
    var titleLocal: String?
    var developer: String?
    var releaseDate: String?
    var coverUri: String?
    var descriptionText: String?
    var rating: Double?
    var vndbId: String?
    var tags: [String]?
    var favorite: Bool
    var playStatus: PlayStatus
    var totalPlayTime: Int64
    var lastPlayedAt: Date?
    var screenshotUris: [String]?
    var categories: [String]?

    enum PlayStatus: String, Codable, CaseIterable {
        case unplayed = "unplayed"
        case playing = "playing"
        case completed = "completed"
        case abandoned = "abandoned"

        var displayName: String {
            switch self {
            case .unplayed: return "未开始"
            case .playing: return "正在玩"
            case .completed: return "已通关"
            case .abandoned: return "已弃坑"
            }
        }
    }

    enum EngineType: String, Codable, CaseIterable {
        case winlator = "winlator"
        case exagear = "exagear"
        case krkr = "krkr"
        case kirikiroid = "kirikiroid"
        case tyranor = "tyranor"
        case other = "other"

        var displayName: String {
            switch self {
            case .winlator: return "Winlator"
            case .exagear: return "ExaGear"
            case .krkr: return "KRKR"
            case .kirikiroid: return "Kirikiroid"
            case .tyranor: return "Tyranor"
            case .other: return "其他"
            }
        }
    }

    func hash(into hasher: inout Hasher) {
        hasher.combine(id)
    }

    static func == (lhs: Game, rhs: Game) -> Bool {
        return lhs.id == rhs.id
    }
}
