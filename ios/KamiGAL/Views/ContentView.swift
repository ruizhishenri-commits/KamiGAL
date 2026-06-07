import SwiftUI

struct ContentView: View {
    @EnvironmentObject var gameStore: GameStore
    @State private var selectedTab = 0
    @State private var showSearch = false

    var body: some View {
        TabView(selection: $selectedTab) {
            LibraryView()
                .tabItem {
                    Image(systemName: "square.grid.2x2")
                    Text("游戏库")
                }
                .tag(0)

            SearchView()
                .tabItem {
                    Image(systemName: "magnifyingglass")
                    Text("搜索")
                }
                .tag(1)

            ToolsView()
                .tabItem {
                    Image(systemName: "wrench.and.screwdriver")
                    Text("工具")
                }
                .tag(2)

            SettingsView()
                .tabItem {
                    Image(systemName: "gearshape")
                    Text("设置")
                }
                .tag(3)
        }
        .accentColor(Color(red: 0.6, green: 0.4, blue: 0.9))
    }
}

struct ToolsView: View {
    var body: some View {
        NavigationStack {
            VStack(spacing: 20) {
                ToolCard(icon: "magnifyingglass.circle", title: "搜索游戏", subtitle: "从VNDB搜索并导入游戏信息", color: .blue)
                ToolCard(icon: "gamecontroller", title: "模拟器管理", subtitle: "管理Winlator、ExaGear等模拟器", color: .green)
                ToolCard(icon: "camera.viewfinder", title: "识图识别", subtitle: "拍照识别Galgame封面", color: .purple)
            }
            .padding()
            .navigationTitle("工具")
        }
    }
}

struct ToolCard: View {
    let icon: String
    let title: String
    let subtitle: String
    let color: Color

    var body: some View {
        HStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 36))
                .foregroundColor(color)
                .frame(width: 60)

            VStack(alignment: .leading, spacing: 4) {
                Text(title)
                    .font(.headline)
                    .foregroundColor(.white)
                Text(subtitle)
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Spacer()

            Image(systemName: "chevron.right")
                .foregroundColor(.gray)
        }
        .padding()
        .background(Color(.systemGray6).opacity(0.15))
        .cornerRadius(12)
    }
}

struct SettingsView: View {
    @AppStorage("vndb_username") private var vndbUsername = ""
    @AppStorage("api_key") private var apiKey = ""
    @State private var showChangelog = false

    var body: some View {
        NavigationStack {
            List {
                Section("VNDB") {
                    TextField("VNDB用户名", text: $vndbUsername)
                    SecureField("API Key", text: $apiKey)
                }

                Section("数据") {
                    Button("导出数据") {}
                    Button("导入数据") {}
                }

                Section("关于") {
                    HStack {
                        Text("版本")
                        Spacer()
                        Text("1.0.5")
                            .foregroundColor(.gray)
                    }
                    Button("更新日志") { showChangelog.toggle() }
                }
            }
            .navigationTitle("设置")
            .alert("更新日志", isPresented: $showChangelog) {
                Button("OK") {}
            } message: {
                Text("v1.0.5\n• iOS版初始版本\n• 支持游戏库管理\n• VNDB搜索集成\n• 收藏与筛选")
            }
        }
    }
}