import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'api.dart';

import 'screens/complaints_tab.dart';
import 'screens/home_tab.dart';
import 'screens/login_screen.dart';
import 'screens/profile_tab.dart';
import 'screens/report_tab.dart';
import 'widgets/common.dart';

void main() {
  runApp(const CivicAiApp());
}

class CivicAiApp extends StatelessWidget {
  const CivicAiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'CivicAI',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        useMaterial3: true,
        brightness: Brightness.dark,
        colorScheme: const ColorScheme.dark(
          primary: AppColors.accent,
          secondary: AppColors.accent2,
          surface: AppColors.card,
        ),
        scaffoldBackgroundColor: AppColors.bgTop,
        fontFamily: 'Segoe UI',
      ),
      home: const AuthGate(),
    );
  }
}

class AuthGate extends StatefulWidget {
  const AuthGate({super.key});

  @override
  State<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends State<AuthGate> {
  Future<SharedPreferences>? _prefs;

  @override
  void initState() {
    super.initState();
    _prefs = SharedPreferences.getInstance();
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder<SharedPreferences>(
      future: _prefs,
      builder: (context, snap) {
        if (!snap.hasData) {
          return const Scaffold(
              body: Center(
                  child: CircularProgressIndicator(color: AppColors.accent)));
        }
final prefs = snap.data!;
        ApiClient.token = prefs.getString('token');
        final email = prefs.getString('email');
        if (email == null) {
          return LoginScreen(onLogin: (name, mail, token, id) async {
            await prefs.setString('name', name);
            await prefs.setString('email', mail);
            await prefs.setString('token', token);
            await prefs.setInt('userId', id);
            ApiClient.token = token;
            setState(() {
              _prefs = SharedPreferences.getInstance();
            });
          });
        }
        return HomeShell(
          key: ValueKey(email),
          name: prefs.getString('name') ?? email.split('@').first,
          email: email,
          userId: prefs.getInt('userId') ?? 0,
        );
      },
    );
  }
}

class HomeShell extends StatefulWidget {
  final String name;
  final String email;
  final int userId;
  const HomeShell(
      {super.key,
      required this.name,
      required this.email,
      required this.userId});

  @override
  State<HomeShell> createState() => _HomeShellState();
}

class _HomeShellState extends State<HomeShell> {
  int _index = 0;
  int _complaintsReloadTick = 0;

  @override
  Widget build(BuildContext context) {
    final screens = [
      HomeTab(name: widget.name, onViewAll: () => setState(() => _index = 2)),
ReportTab(
          userId: widget.userId,
          onSubmitted: () => setState(() {
                _index = 2;
                _complaintsReloadTick++;
              })),
      ComplaintsTab(key: ValueKey(_complaintsReloadTick)),
      ProfileTab(
          name: widget.name,
          email: widget.email,
          onLogout: () => setState(() {})),
    ];
    return Container(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [AppColors.bgTop, AppColors.bgBottom],
        ),
      ),
      child: Scaffold(
        backgroundColor: Colors.transparent,
        body: AnimatedSwitcher(
          duration: const Duration(milliseconds: 250),
          child: KeyedSubtree(
              key: ValueKey(_index), child: screens[_index]),
        ),
        bottomNavigationBar: NavigationBar(
          backgroundColor: AppColors.card.withValues(alpha: .96),
          indicatorColor: AppColors.accent.withValues(alpha: .18),
          height: 68,
          selectedIndex: _index,
          onDestinationSelected: (i) => setState(() => _index = i),
          destinations: const [
            NavigationDestination(
                icon: Icon(Icons.space_dashboard_outlined),
                selectedIcon:
                    Icon(Icons.dashboard_rounded, color: AppColors.accent),
                label: 'Home'),
            NavigationDestination(
                icon: Icon(Icons.add_circle_outline_rounded),
                selectedIcon:
                    Icon(Icons.add_circle_rounded, color: AppColors.accent),
                label: 'Report'),
            NavigationDestination(
                icon: Icon(Icons.fact_check_outlined),
                selectedIcon:
                    Icon(Icons.fact_check_rounded, color: AppColors.accent),
                label: 'Reports'),
            NavigationDestination(
                icon: Icon(Icons.person_outline_rounded),
                selectedIcon:
                    Icon(Icons.person_rounded, color: AppColors.accent),
                label: 'Profile'),
          ],
        ),
      ),
    );
  }
}




