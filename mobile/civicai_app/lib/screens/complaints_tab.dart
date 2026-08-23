import 'package:flutter/material.dart';

import '../api.dart';
import '../widgets/common.dart';

class ComplaintsTab extends StatefulWidget {
  const ComplaintsTab({super.key});

  @override
  State<ComplaintsTab> createState() => _ComplaintsTabState();
}

class _ComplaintsTabState extends State<ComplaintsTab> {
  final _api = ApiClient();
  List<Complaint>? _complaints;
  String _query = '';

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final list = await _api.fetchComplaints();
      if (!mounted) return;
      setState(() => _complaints = list);
    } catch (e) {
      if (!mounted) return;
      setState(() => _complaints = []);
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
          backgroundColor: AppColors.card, content: Text('Load failed: $e')));
    }
  }

  @override
  Widget build(BuildContext context) {
    final all = _complaints;
    final filtered = (all ?? [])
        .where((c) =>
            c.title.toLowerCase().contains(_query) ||
            c.issueType.toLowerCase().contains(_query) ||
            c.status.toLowerCase().contains(_query))
        .toList()
      ..sort((a, b) => (b.createdAt ?? DateTime(2000))
          .compareTo(a.createdAt ?? DateTime(2000)));

    return Column(children: [
      Padding(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 10),
        child: TextField(
          onChanged: (v) => setState(() => _query = v.toLowerCase()),
          style: const TextStyle(color: Colors.white),
          decoration: InputDecoration(
            hintText: 'Search reports…',
            hintStyle: const TextStyle(color: AppColors.muted),
            prefixIcon:
                const Icon(Icons.search_rounded, color: AppColors.accent),
            filled: true,
            fillColor: AppColors.card,
            enabledBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(16),
                borderSide: const BorderSide(color: AppColors.cardBorder)),
            focusedBorder: OutlineInputBorder(
                borderRadius: BorderRadius.circular(16),
                borderSide: const BorderSide(color: AppColors.accent)),
          ),
        ),
      ),
      Expanded(
        child: all == null
            ? const Center(
                child: CircularProgressIndicator(color: AppColors.accent))
            : RefreshIndicator(
                color: AppColors.accent,
                onRefresh: _load,
                child: filtered.isEmpty
                    ? ListView(children: const [
                        SizedBox(height: 140),
                        Icon(Icons.inbox_rounded,
                            size: 56, color: AppColors.muted),
                        SizedBox(height: 12),
                        Center(
                            child: Text('No reports found',
                                style: TextStyle(color: AppColors.muted))),
                      ])
                    : ListView.separated(
                        padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
                        itemCount: filtered.length,
                        separatorBuilder: (context, index) => const SizedBox(height: 10),
                        itemBuilder: (_, i) => ComplaintTile(
                            complaint: filtered[i],
                            onTap: () => _openDetail(filtered[i]))),
              ),
      ),
    ]);
  }

  void _openDetail(Complaint c) {
    showModalBottomSheet(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => _DetailSheet(complaint: c),
    );
  }
}

class ComplaintTile extends StatelessWidget {
  final Complaint complaint;
  final VoidCallback? onTap;
  const ComplaintTile({super.key, required this.complaint, this.onTap});

  @override
  Widget build(BuildContext context) {
    final data = AppColors.issueData[complaint.issueType] ??
        AppColors.issueData['OTHER']!;
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(18),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: AppColors.cardBorder)),
          child: Row(children: [
            Container(
              width: 44,
              height: 44,
              decoration: BoxDecoration(
                  color: data.$2.withValues(alpha: .15),
                  borderRadius: BorderRadius.circular(14)),
              child: Icon(data.$1, color: data.$2, size: 22),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(complaint.title,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.w700,
                            fontSize: 14)),
                    const SizedBox(height: 3),
                    Text(
                      '#${complaint.id} · ${complaint.issueType.replaceAll('_', ' ')}'
                      '${complaint.duplicateOfId != null ? ' · dup of #${complaint.duplicateOfId}' : ''}'
                      ' · ${fmtDate(complaint.createdAt)}',
                      style:
                          const TextStyle(color: AppColors.muted, fontSize: 11),
                    ),
                  ]),
            ),
            const SizedBox(width: 8),
            PriorityBadge(priority: complaint.priority),
            const SizedBox(width: 8),
            StatusChip(status: complaint.status),
          ]),
        ),
      ),
    );
  }
}

class _DetailSheet extends StatelessWidget {
  final Complaint complaint;
  const _DetailSheet({required this.complaint});

  @override
  Widget build(BuildContext context) {
    final c = complaint;
    final currentIdx = statusOrder.indexOf(c.status);
    return Container(
      margin: const EdgeInsets.only(top: 40),
      padding: const EdgeInsets.fromLTRB(22, 18, 22, 30),
      decoration: const BoxDecoration(
        color: AppColors.bgBottom,
        borderRadius: BorderRadius.vertical(top: Radius.circular(28)),
        border: Border(top: BorderSide(color: AppColors.cardBorder)),
      ),
      child: Column(mainAxisSize: MainAxisSize.min, crossAxisAlignment:
          CrossAxisAlignment.start, children: [
        Center(
          child: Container(
              width: 42,
              height: 4,
              decoration: BoxDecoration(
                  color: AppColors.muted.withValues(alpha: .4),
                  borderRadius: BorderRadius.circular(2))),
        ),
        const SizedBox(height: 16),
        Row(children: [
          Expanded(
              child: Text(c.title,
                  style: const TextStyle(
                      color: Colors.white,
                      fontSize: 19,
                      fontWeight: FontWeight.w800))),
          PriorityBadge(priority: c.priority),
          const SizedBox(width: 8),
          StatusChip(status: c.status),
        ]),
        if (c.description != null && c.description!.isNotEmpty) ...[
          const SizedBox(height: 10),
          Text(c.description!,
              style: const TextStyle(color: Colors.white70, fontSize: 13.5)),
        ],
        const SizedBox(height: 16),
        Wrap(spacing: 8, runSpacing: 6, children: [
          _meta(Icons.tag_rounded, 'ID ${c.id}'),
          _meta(Icons.category_rounded, c.issueType.replaceAll('_', ' ')),
          if (c.departmentId != null)
            _meta(Icons.apartment_rounded, 'Dept ${c.departmentId}'),
          if (c.clusterId != null)
            _meta(Icons.workspaces_rounded, 'Cluster ${c.clusterId}'),
          if (c.duplicateOfId != null)
            _meta(Icons.copy_all_rounded,
                'Duplicate of #${c.duplicateOfId} (${((c.duplicateScore ?? 0) * 100).toStringAsFixed(0)}%)'),
          _meta(Icons.place_rounded,
              '${c.latitude.toStringAsFixed(4)}, ${c.longitude.toStringAsFixed(4)}'),
        ]),
        const SizedBox(height: 22),
        const Text('Progress',
            style: TextStyle(
                color: Colors.white, fontSize: 15, fontWeight: FontWeight.w700)),
        const SizedBox(height: 12),
        ...statusOrder.map((s) {
          final idx = statusOrder.indexOf(s);
          final done = currentIdx >= idx && c.status != 'REJECTED';
          final isLast = idx == statusOrder.length - 1;
          return IntrinsicHeight(
            child: Row(crossAxisAlignment: CrossAxisAlignment.stretch, children: [
              Column(children: [
                Container(
                  width: 26,
                  height: 26,
                  decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: done
                          ? AppColors.accent
                          : AppColors.card,
                      border: Border.all(
                          color: done ? AppColors.accent : AppColors.cardBorder)),
                  child: done
                      ? const Icon(Icons.check_rounded,
                          size: 16, color: Colors.black)
                      : null,
                ),
                if (!isLast)
                  Expanded(
                      child: Container(
                          width: 2,
                          color: currentIdx > idx
                              ? AppColors.accent
                              : AppColors.cardBorder)),
              ]),
              const SizedBox(width: 12),
              Padding(
                padding: const EdgeInsets.only(bottom: 14, top: 3),
                child: Text(s.replaceAll('_', ' '),
                    style: TextStyle(
                        fontSize: 13,
                        fontWeight:
                            done ? FontWeight.w700 : FontWeight.w400,
                        color: done ? Colors.white : AppColors.muted)),
              ),
            ]),
          );
        }),
      ]),
    );
  }

  Widget _meta(IconData i, String t) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
        decoration: BoxDecoration(
            color: AppColors.card,
            borderRadius: BorderRadius.circular(10),
            border: Border.all(color: AppColors.cardBorder)),
        child: Row(mainAxisSize: MainAxisSize.min, children: [
          Icon(i, size: 14, color: AppColors.accent),
          const SizedBox(width: 5),
          Text(t, style: const TextStyle(color: Colors.white70, fontSize: 11.5)),
        ]),
      );
}


