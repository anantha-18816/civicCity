import 'package:flutter/material.dart';

import '../api.dart';
import '../widgets/common.dart';
import 'complaints_tab.dart';

class HomeTab extends StatefulWidget {
  final String name;
  final VoidCallback onViewAll;
  const HomeTab({super.key, required this.name, required this.onViewAll});

  @override
  State<HomeTab> createState() => _HomeTabState();
}

class _HomeTabState extends State<HomeTab> {
  final _api = ApiClient();
  List<Complaint>? _complaints;
  AiInsights? _insights;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    setState(() => _error = null);
    try {
      final results = await Future.wait(
          [_api.fetchComplaints(), _api.fetchInsights()]);
      if (!mounted) return;
      setState(() {
        _complaints = results[0] as List<Complaint>;
        _insights = results[1] as AiInsights;
      });
    } catch (e) {
      if (mounted) setState(() => _error = e.toString());
    }
  }

  @override
  Widget build(BuildContext context) {
    final complaints = _complaints;
    return RefreshIndicator(
      color: AppColors.accent,
      onRefresh: _load,
      child: ListView(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
        children: [
          Row(
            children: [
              Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                Text('Hey ${widget.name} 👋',
                    style: const TextStyle(
                        color: AppColors.muted, fontSize: 13)),
                const SizedBox(height: 2),
                const Text('City Dashboard',
                    style: TextStyle(
                        color: Colors.white,
                        fontSize: 24,
                        fontWeight: FontWeight.w800)),
              ]),
              const Spacer(),
              Container(
                padding: const EdgeInsets.all(10),
                decoration: BoxDecoration(
                    color: AppColors.card,
                    shape: BoxShape.circle,
                    border: Border.all(color: AppColors.cardBorder)),
                child: const Icon(Icons.notifications_none_rounded,
                    color: AppColors.accent),
              ),
            ],
          ),
          const SizedBox(height: 20),
          if (_error != null)
            _errorCard()
          else if (complaints == null)
            const SizedBox(
                height: 300,
                child: Center(
                    child: CircularProgressIndicator(color: AppColors.accent)))
          else ...[
            GridView.count(
              crossAxisCount: 2,
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              mainAxisSpacing: 12,
              crossAxisSpacing: 12,
              childAspectRatio: 1.45,
              children: [
                StatCard(
                    label: 'Total reports',
                    value: '${complaints.length}',
                    icon: Icons.campaign_rounded,
                    color: AppColors.accent),
                StatCard(
                    label: 'Open',
                    value:
                        '${complaints.where((c) => c.status != 'RESOLVED' && c.status != 'VERIFIED' && c.status != 'REJECTED').length}',
                    icon: Icons.timelapse_rounded,
                    color: Colors.amber),
                StatCard(
                    label: 'Resolved',
                    value:
                        '${complaints.where((c) => c.status == 'RESOLVED' || c.status == 'VERIFIED').length}',
                    icon: Icons.verified_rounded,
                    color: AppColors.successGreen),
                StatCard(
                    label: 'Dupes linked',
                    value: '${_insights?.duplicatesLinked ?? '-'}',
                    icon: Icons.copy_all_rounded,
                    color: AppColors.accent2),
              ],
            ),
            const SizedBox(height: 20),
            _aiCard(),
            const SizedBox(height: 20),
            Row(mainAxisAlignment: MainAxisAlignment.spaceBetween, children: [
              const Text('Recent reports',
                  style: TextStyle(
                      color: Colors.white,
                      fontSize: 17,
                      fontWeight: FontWeight.w700)),
              TextButton(
                  onPressed: widget.onViewAll,
                  child: const Text('View all',
                      style: TextStyle(color: AppColors.accent))),
            ]),
            ...complaints
                .sortedByAge()
                .take(3)
                .map((c) => Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: ComplaintTile(complaint: c),
                    )),
          ],
        ],
      ),
    );
  }

  Widget _errorCard() => Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
            color: AppColors.card,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: AppColors.cardBorder)),
        child: Column(children: [
          const Icon(Icons.wifi_off_rounded, color: Colors.redAccent, size: 34),
          const SizedBox(height: 10),
          const Text('Cannot reach backend at ${ApiClient.baseUrl}',
              textAlign: TextAlign.center,
              style: TextStyle(color: Colors.white70, fontSize: 13)),
          const SizedBox(height: 6),
          Text(_error!, style: const TextStyle(color: AppColors.muted, fontSize: 11)),
          const SizedBox(height: 10),
          OutlinedButton.icon(
              onPressed: _load,
              icon: const Icon(Icons.refresh, size: 18),
              label: const Text('Retry'),
              style: OutlinedButton.styleFrom(
                  foregroundColor: AppColors.accent,
                  side: const BorderSide(color: AppColors.accent))),
        ]),
      );

  Widget _aiCard() {
    final i = _insights;
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(20),
        gradient: LinearGradient(colors: [
          AppColors.accent.withValues(alpha: .12),
          AppColors.accent2.withValues(alpha: .10),
        ]),
        border: Border.all(color: AppColors.cardBorder),
      ),
      child: Row(children: [
        Container(
          padding: const EdgeInsets.all(10),
          decoration: BoxDecoration(
              color: AppColors.accent.withValues(alpha: .15),
              shape: BoxShape.circle),
          child: const Icon(Icons.psychology_alt_rounded,
              color: AppColors.accent, size: 26),
        ),
        const SizedBox(width: 14),
        Expanded(
          child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('AI engine live',
                    style: TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.w700,
                        fontSize: 14)),
                const SizedBox(height: 3),
                Text(
                  i == null
                      ? 'Loading model stats…'
                      : '${i.totalAnalyses} analyses · avg confidence ${(i.avgConfidence * 100).toStringAsFixed(0)}% · top: ${i.detectionsByObject.keys.fold('', (a, b) => a.isEmpty || i.detectionsByObject[b]! > i.detectionsByObject[a]! ? b : a)}',
                  style: const TextStyle(color: AppColors.muted, fontSize: 11.5),
                ),
              ]),
        ),
      ]),
    );
  }
}

extension on List<Complaint> {
  List<Complaint> sortedByAge() {
    final copy = [...this];
    copy.sort((a, b) => (b.createdAt ?? DateTime(2000))
        .compareTo(a.createdAt ?? DateTime(2000)));
    return copy;
  }
}
