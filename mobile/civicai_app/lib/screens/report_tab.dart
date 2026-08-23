import 'dart:io';

import 'package:flutter/foundation.dart' show kIsWeb;
import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';

import '../api.dart';
import '../widgets/common.dart';

class ReportTab extends StatefulWidget {
  final int userId;
  final VoidCallback onSubmitted;
  const ReportTab(
      {super.key, required this.userId, required this.onSubmitted});

  @override
  State<ReportTab> createState() => _ReportTabState();
}

class _ReportTabState extends State<ReportTab> {
  final _api = ApiClient();
  final _title = TextEditingController();
  final _desc = TextEditingController();
  final _lat = TextEditingController();
  final _lng = TextEditingController();
  String _issue = 'POTHOLE';
  bool _locating = false;
  bool _submitting = false;

  static const issues = [
    'POTHOLE',
    'GARBAGE',
    'STREETLIGHT',
    'WATER_LOGGING',
    'ROAD_DAMAGE',
    'OTHER'
  ];

  Future<void> _captureGps() async {
    setState(() => _locating = true);
    try {
      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }
      if (permission == LocationPermission.denied ||
          permission == LocationPermission.deniedForever) {
        throw Exception('Location permission denied');
      }
      final p = await Geolocator.getCurrentPosition(
          locationSettings:
              const LocationSettings(accuracy: LocationAccuracy.high));
      _lat.text = p.latitude.toStringAsFixed(6);
      _lng.text = p.longitude.toStringAsFixed(6);
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            backgroundColor: AppColors.card,
            content: Text('GPS unavailable: $e — enter manually')));
      }
    }
    if (mounted) setState(() => _locating = false);
  }

  Future<void> _submit() async {
    final lat = double.tryParse(_lat.text.trim());
    final lng = double.tryParse(_lng.text.trim());
    if (_title.text.trim().isEmpty || lat == null || lng == null) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
          backgroundColor: AppColors.card,
          content: Text('Title and coordinates are required')));
      return;
    }
    setState(() => _submitting = true);
    try {
      await _api.createComplaint(
        userId: widget.userId,
        issueType: _issue,
        title: _title.text.trim(),
        description: _desc.text.trim(),
        latitude: lat,
        longitude: lng,
      );
      if (!mounted) return;
      _title.clear();
      _desc.clear();
      showDialog(
        context: context,
        builder: (_) => AlertDialog(
          backgroundColor: AppColors.card,
          shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(24)),
          title: const Icon(Icons.check_circle_rounded,
              color: AppColors.successGreen, size: 54),
          content: const Text(
            'Report submitted!\nAI analysis will run shortly.',
            textAlign: TextAlign.center,
            style: TextStyle(color: Colors.white70),
          ),
          actions: [
            FilledButton(
                style: FilledButton.styleFrom(
                    backgroundColor: AppColors.accent,
                    foregroundColor: Colors.black),
                onPressed: () {
                  Navigator.pop(context);
                  widget.onSubmitted();
                },
                child: const Text('Track it'))
          ],
        ),
      );
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(SnackBar(
            backgroundColor: AppColors.card,
            content: Text('Submit failed: $e')));
      }
    }
    if (mounted) setState(() => _submitting = false);
  }

  @override
  Widget build(BuildContext context) {
    final mobileCamera =
        !kIsWeb && (Platform.isAndroid || Platform.isIOS);
    return ListView(
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
      children: [
        const Text('Report an issue',
            style: TextStyle(
                color: Colors.white,
                fontSize: 24,
                fontWeight: FontWeight.w800)),
        const SizedBox(height: 4),
        const Text('Snap · pin · send. AI does the triage.',
            style: TextStyle(color: AppColors.muted, fontSize: 13)),
        const SizedBox(height: 20),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: issues.map((i) {
            final data = AppColors.issueData[i]!;
            final selected = i == _issue;
            return ChoiceChip(
              selected: selected,
              onSelected: (_) => setState(() => _issue = i),
              avatar: Icon(data.$1,
                  size: 18,
                  color: selected ? Colors.black : data.$2),
              label: Text(i.replaceAll('_', ' '),
                  style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: selected ? Colors.black : Colors.white70)),
              selectedColor: AppColors.accent,
              backgroundColor: AppColors.card,
              side: BorderSide(
                  color: selected
                      ? AppColors.accent
                      : AppColors.cardBorder),
            );
          }).toList(),
        ),
        const SizedBox(height: 18),
        _input(_title, 'Title *', Icons.title_rounded),
        const SizedBox(height: 12),
        _input(_desc, 'Describe the problem', Icons.notes_rounded,
            maxLines: 3),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.cardBorder)),
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            Row(children: [
              const Icon(Icons.my_location_rounded,
                  color: AppColors.accent, size: 18),
              const SizedBox(width: 8),
              const Text('Location',
                  style: TextStyle(
                      color: Colors.white, fontWeight: FontWeight.w700)),
              const Spacer(),
              _locating
                  ? const SizedBox(
                      width: 16,
                      height: 16,
                      child: CircularProgressIndicator(strokeWidth: 2))
                  : TextButton.icon(
                      onPressed: _captureGps,
                      icon: const Icon(Icons.gps_fixed_rounded, size: 16),
                      label: const Text('Use GPS'),
                      style: TextButton.styleFrom(
                          foregroundColor: AppColors.accent,
                          textStyle: const TextStyle(fontSize: 12))),
            ]),
            Row(children: [
              Expanded(
                  child: TextField(
                      controller: _lat,
                      keyboardType: TextInputType.number,
                      style: const TextStyle(color: Colors.white, fontSize: 13),
                      decoration: _mini('Latitude'))),
              const SizedBox(width: 10),
              Expanded(
                  child: TextField(
                      controller: _lng,
                      keyboardType: TextInputType.number,
                      style: const TextStyle(color: Colors.white, fontSize: 13),
                      decoration: _mini('Longitude'))),
            ]),
          ]),
        ),
        const SizedBox(height: 12),
        Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
              color: AppColors.card,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.cardBorder)),
          child: Row(children: [
            Icon(Icons.photo_camera_rounded,
                color: mobileCamera ? AppColors.accent : AppColors.muted),
            const SizedBox(width: 10),
            Expanded(
                child: Text(
                    mobileCamera
                        ? 'Attach a photo from your camera'
                        : 'Photo attach is enabled on Android/iOS devices',
                    style:
                        const TextStyle(color: Colors.white70, fontSize: 12.5))),
            if (mobileCamera)
              const Icon(Icons.arrow_forward_ios_rounded,
                  size: 14, color: AppColors.muted),
          ]),
        ),
        const SizedBox(height: 22),
        SizedBox(
          height: 52,
          child: DecoratedBox(
            decoration: BoxDecoration(
              borderRadius: BorderRadius.circular(16),
              gradient: const LinearGradient(
                  colors: [AppColors.accent, AppColors.accent2]),
            ),
            child: ElevatedButton.icon(
              style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.transparent,
                  shadowColor: Colors.transparent,
                  foregroundColor: Colors.black,
                  shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16))),
              onPressed: _submitting ? null : _submit,
              icon: _submitting
                  ? const SizedBox(
                      width: 18,
                      height: 18,
                      child: CircularProgressIndicator(strokeWidth: 2))
                  : const Icon(Icons.send_rounded),
              label: Text(_submitting ? 'Submitting…' : 'Submit report',
                  style: const TextStyle(fontWeight: FontWeight.w800)),
            ),
          ),
        ),
      ],
    );
  }

  InputDecoration _mini(String h) => InputDecoration(
        hintText: h,
        hintStyle: const TextStyle(color: AppColors.muted, fontSize: 12),
        filled: true,
        fillColor: AppColors.bgTop,
        contentPadding:
            const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        enabledBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: AppColors.cardBorder)),
        focusedBorder: OutlineInputBorder(
            borderRadius: BorderRadius.circular(12),
            borderSide: const BorderSide(color: AppColors.accent)),
      );

  Widget _input(TextEditingController c, String h, IconData icon,
          {int maxLines = 1}) =>
      TextField(
        controller: c,
        maxLines: maxLines,
        style: const TextStyle(color: Colors.white),
        decoration: InputDecoration(
          hintText: h,
          hintStyle: const TextStyle(color: AppColors.muted),
          prefixIcon:
              maxLines == 1 ? Icon(icon, color: AppColors.accent, size: 20) : null,
          filled: true,
          fillColor: AppColors.card,
          enabledBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(16),
              borderSide: const BorderSide(color: AppColors.cardBorder)),
          focusedBorder: OutlineInputBorder(
              borderRadius: BorderRadius.circular(16),
              borderSide: const BorderSide(color: AppColors.accent)),
        ),
      );
}
