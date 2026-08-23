import 'package:flutter/material.dart';

class AppColors {
  static const bgTop = Color(0xFF0B1220);
  static const bgBottom = Color(0xFF111C2E);
  static const card = Color(0xFF16233A);
  static const cardBorder = Color(0xFF24344F);
  static const accent = Color(0xFF22D3EE);
  static const accent2 = Color(0xFFA78BFA);
  static const muted = Color(0xFF8CA3C3);
  static const successGreen = Color(0xFF34D399);

  static const statusColors = {
    'SUBMITTED': Color(0xFFFBBF24),
    'AI_ANALYZED': Color(0xFF60A5FA),
    'ASSIGNED': Color(0xFFA78BFA),
    'IN_PROGRESS': Color(0xFFFB923C),
    'RESOLVED': Color(0xFF34D399),
    'REJECTED': Color(0xFFF87171),
    'VERIFIED': Color(0xFF2DD4BF),
  };

  static const priorityColors = {
    1: Color(0xFFF87171),
    2: Color(0xFFFB923C),
    3: Color(0xFFFBBF24),
    4: Color(0xFF64748B),
  };

  static const issueData = {
    'POTHOLE': (Icons.construction_rounded, Color(0xFFFB923C)),
    'GARBAGE': (Icons.delete_outline_rounded, Color(0xFF34D399)),
    'STREETLIGHT': (Icons.light_mode_outlined, Color(0xFFFBBF24)),
    'WATER_LOGGING': (Icons.water_drop_outlined, Color(0xFF60A5FA)),
    'ROAD_DAMAGE': (Icons.broken_image_outlined, Color(0xFFF87171)),
    'OTHER': (Icons.report_problem_outlined, Color(0xFFA78BFA)),
  };
}

const statusOrder = [
  'SUBMITTED',
  'AI_ANALYZED',
  'ASSIGNED',
  'IN_PROGRESS',
  'RESOLVED',
  'VERIFIED',
];

class StatusChip extends StatelessWidget {
  final String status;
  const StatusChip({super.key, required this.status});

  @override
  Widget build(BuildContext context) {
    final c = AppColors.statusColors[status] ?? AppColors.muted;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: c.withValues(alpha: 0.14),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: c.withValues(alpha: 0.5)),
      ),
      child: Text(
        status.replaceAll('_', ' '),
        style: TextStyle(
            color: c, fontSize: 11, fontWeight: FontWeight.w700, letterSpacing: .4),
      ),
    );
  }
}

class PriorityBadge extends StatelessWidget {
  final int? priority;
  const PriorityBadge({super.key, this.priority});

  @override
  Widget build(BuildContext context) {
    if (priority == null) return const SizedBox.shrink();
    final c = AppColors.priorityColors[priority] ?? AppColors.muted;
    return Container(
      width: 30,
      height: 30,
      alignment: Alignment.center,
      decoration:
          BoxDecoration(color: c.withValues(alpha: 0.16), shape: BoxShape.circle),
      child: Text('P$priority',
          style: TextStyle(
              color: c, fontSize: 11, fontWeight: FontWeight.w800)),
    );
  }
}

class StatCard extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;
  final Color color;
  const StatCard({
    super.key,
    required this.label,
    required this.value,
    required this.icon,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: AppColors.card,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: AppColors.cardBorder),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(icon, color: color, size: 22),
          const Spacer(),
          Text(value,
              style: const TextStyle(
                  fontSize: 22, fontWeight: FontWeight.w800, color: Colors.white)),
          const SizedBox(height: 2),
          Text(label,
              style: const TextStyle(fontSize: 11, color: AppColors.muted)),
        ],
      ),
    );
  }
}

String fmtDate(DateTime? dt) {
  if (dt == null) return '';
  final l = dt.toLocal();
  const m = [
    'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
    'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
  ];
  final h = l.hour.toString().padLeft(2, '0');
  final min = l.minute.toString().padLeft(2, '0');
  return '${l.day} ${m[l.month - 1]} · $h:$min';
}
