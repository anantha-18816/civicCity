import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../api.dart';
import '../widgets/common.dart';

class ProfileTab extends StatefulWidget {
  final String name;
  final String email;
  final VoidCallback onLogout;
  const ProfileTab(
      {super.key,
      required this.name,
      required this.email,
      required this.onLogout});

  @override
  State<ProfileTab> createState() => _ProfileTabState();
}

class _ProfileTabState extends State<ProfileTab> {
  int _myReports = 0;
  final _api = ApiClient();

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final list = await _api.fetchComplaints();
      if (!mounted) return;
      setState(() => _myReports = list.length);
    } catch (_) {}
  }

  @override
  Widget build(BuildContext context) {
    final initials =
        widget.name.isEmpty ? 'C' : widget.name[0].toUpperCase();
    return ListView(padding: const EdgeInsets.fromLTRB(20, 20, 20, 30), children: [
      Column(children: [
        Container(
          width: 92,
          height: 92,
          alignment: Alignment.center,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            gradient:
                const LinearGradient(colors: [AppColors.accent, AppColors.accent2]),
            boxShadow: [
              BoxShadow(color: AppColors.accent2.withValues(alpha: .3), blurRadius: 26)
            ],
          ),
          child: Text(initials,
              style: const TextStyle(
                  fontSize: 36, fontWeight: FontWeight.w900, color: Colors.black)),
        ),
        const SizedBox(height: 14),
        Text(widget.name,
            style: const TextStyle(
                color: Colors.white, fontSize: 21, fontWeight: FontWeight.w800)),
        Text(widget.email,
            style: const TextStyle(color: AppColors.muted, fontSize: 13)),
      ]),
      const SizedBox(height: 24),
      Row(children: [
        Expanded(
            child: StatCard(
                label: 'Reports tracked',
                value: '$_myReports',
                icon: Icons.campaign_rounded,
                color: AppColors.accent)),
        const SizedBox(width: 12),
        const Expanded(
            child: StatCard(
                label: 'City rank',
                value: 'Active',
                icon: Icons.emoji_events_rounded,
                color: Colors.amber)),
      ]),
      const SizedBox(height: 24),
      Container(
        decoration: BoxDecoration(
            color: AppColors.card,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: AppColors.cardBorder)),
        child: Column(children: [
          ListTile(
              leading: const Icon(Icons.notifications_none_rounded,
                  color: AppColors.accent),
              title: const Text('Notifications',
                  style: TextStyle(color: Colors.white, fontSize: 14)),
              trailing: Switch(
                  value: true,
                  activeThumbColor: AppColors.accent,
                  onChanged: (_) {})),
          const Divider(height: 1, color: AppColors.cardBorder),
          const ListTile(
              leading:
                  Icon(Icons.psychology_alt_rounded, color: AppColors.accent2),
              title: Text('Powered by CivicAI CV engine v0.2.0',
                  style: TextStyle(color: Colors.white70, fontSize: 13))),
        ]),
      ),
      const SizedBox(height: 22),
      SizedBox(
        height: 50,
        child: OutlinedButton.icon(
          style: OutlinedButton.styleFrom(
              foregroundColor: Colors.redAccent,
              side: BorderSide(color: Colors.redAccent.withValues(alpha: .5)),
              shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(15))),
          onPressed: () async {
            final prefs = await SharedPreferences.getInstance();
            await prefs.clear();
            widget.onLogout();
          },
          icon: const Icon(Icons.logout_rounded, size: 18),
          label: const Text('Sign out'),
        ),
      ),
    ]);
  }
}

