import Foundation

class VNDBService {
    static let shared = VNDBService()

    private let baseURL = "https://api.vndb.org/kana"

    enum VNDBError: Error {
        case networkError(Error)
        case decodingError(Error)
        case noResults
        case invalidResponse
    }

    func searchGame(query: String) async throws -> [VNDBResult] {
        let url = URL(string: "\(baseURL)/vn")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("KamiGAL-iOS/1.0", forHTTPHeaderField: "User-Agent")

        let body: [String: Any] = [
            "filters": ["search", "~", query],
            "fields": "id,title,image.url,description,released,rating,average,developers.name,tags.name",
            "results": 20,
            "sort": "searchrank"
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        let decoder = JSONDecoder()
        let response = try decoder.decode(VNDBResponse.self, from: data)
        return response.results
    }

    func fetchScreenshots(vndbId: String) async throws -> [String] {
        let url = URL(string: "\(baseURL)/vn")!
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")

        let body: [String: Any] = [
            "filters": ["id", "=", vndbId],
            "fields": "screenshots.url",
            "results": 1
        ]
        request.httpBody = try JSONSerialization.data(withJSONObject: body)

        let (data, _) = try await URLSession.shared.data(for: request)
        let response = try JSONDecoder().decode(VNDBResponse.self, from: data)
        return response.results.first?.screenshots?.compactMap { $0.url } ?? []
    }
}

struct VNDBResponse: Codable {
    let results: [VNDBResult]
}

struct VNDBResult: Codable, Identifiable {
    let id: String
    let title: String?
    let image: VNDBImage?
    let description: String?
    let released: String?
    let rating: Double?
    let average: Double?
    let developers: [VNDBDeveloper]?
    let tags: [VNDBTag]?
    let screenshots: [VNDBScreenshot]?

    var displayRating: String {
        if let r = average { return String(format: "%.1f", r) }
        if let r = rating { return String(format: "%.1f", r) }
        return "N/A"
    }
}

struct VNDBImage: Codable {
    let url: String?
}

struct VNDBDeveloper: Codable {
    let name: String?
}

struct VNDBTag: Codable {
    let name: String?
}

struct VNDBScreenshot: Codable {
    let url: String?
}