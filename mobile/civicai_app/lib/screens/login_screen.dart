import 'package:flutter/material.dart';

import '../widgets/common.dart';

class LoginScreen extends StatefulWidget {
  final void Function(String name, String email) onLogin;
  const LoginScreen({super.key, required this.onLogin});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen>
    with SingleTickerProviderStateMixin {
  final _email = TextEditingController();
  final _password = TextEditingController();
  bool _loading = false;
  final bool _obscure = true;
  late final AnimationController _glow =
      AnimationController(vsync: this, duration: const Duration(seconds: 3))
        ..repeat(reverse: true);

  void _login() async {
    final email = _email.text.trim();
    if (!email.contains('@') || _password.text.length < 4) {
      ScaffoldMessenger.of(context).showSnackBar(const SnackBar(
          content: Text('Enter a valid email and password (4+ chars)'),
          backgroundColor: AppColors.card));
      return;
    }
    setState(() => _loading = true);
    await Future.delayed(const Duration(milliseconds: 600));
    if (!mounted) return;
    widget.onLogin(email.split('@').first, email);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Container(
        decoration: const BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [AppColors.bgTop, AppColors.bgBottom],
          ),
        ),
        child: SafeArea(
          child: Center(
            child: SingleChildScrollView(
              padding: const EdgeInsets.symmetric(horizontal: 28),
              child: Column(
                children: [
                  FadeTransition(
                    opacity: Tween(begin: 0.6, end: 1.0).animate(CurvedAnimation(
                        parent: _glow, curve: Curves.easeInOut)),
                    child: Container(
                      width: 96,
                      height: 96,
                      decoration: BoxDecoration(
                        shape: BoxShape.circle,
                        gradient: const LinearGradient(colors: [
                          AppColors.accent,
                          AppColors.accent2
                        ]),
                        boxShadow: [
                          BoxShadow(
                              color:
                                  AppColors.accent.withValues(alpha: .35),
                              blurRadius: 40,
                              spreadRadius: 4),
                        ],
                      ),
                      child: const Icon(Icons.location_city_rounded,
                          size: 46, color: Colors.white),
                    ),
                  ),
                  const SizedBox(height: 24),
                  const Text('CivicAI',
                      style: TextStyle(
                          fontSize: 34,
                          fontWeight: FontWeight.w900,
                          color: Colors.white,
                          letterSpacing: 1.2)),
                  const SizedBox(height: 6),
                  const Text('Your city, reported and resolved.',
                      style: TextStyle(color: AppColors.muted, fontSize: 14)),
                  const SizedBox(height: 40),
                  _field(_email, 'Email', Icons.alternate_email_rounded,
                      false, TextInputType.emailAddress),
                  const SizedBox(height: 14),
                  _field(_password, 'Password', Icons.lock_outline_rounded,
                      _obscure, TextInputType.visiblePassword),
                  const SizedBox(height: 30),
                  SizedBox(
                    width: double.infinity,
                    height: 54,
                    child: DecoratedBox(
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(18),
                        gradient: const LinearGradient(colors: [
                          AppColors.accent,
                          AppColors.accent2
                        ]),
                        boxShadow: [
                          BoxShadow(
                              color:
                                  AppColors.accent2.withValues(alpha: .3),
                              blurRadius: 20,
                              offset: const Offset(0, 6)),
                        ],
                      ),
                      child: ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.transparent,
                            shadowColor: Colors.transparent,
                            foregroundColor: Colors.white,
                            shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(18))),
                        onPressed: _loading ? null : _login,
                        icon: _loading
                            ? const SizedBox(
                                width: 20,
                                height: 20,
                                child: CircularProgressIndicator(
                                    strokeWidth: 2, color: Colors.white))
                            : const Icon(Icons.arrow_forward_rounded),
                        label: Text(_loading ? 'Signing in' : 'Continue',
                            style: const TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w700)),
                      ),
                    ),
                  ),
                  const SizedBox(height: 22),
                  const Text('Demo mode · real auth arrives in Phase 11',
                      style: TextStyle(color: AppColors.muted, fontSize: 11)),
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _field(TextEditingController c, String hint, IconData icon,
          bool obscure, TextInputType type) =>
      TextField(
        controller: c,
        obscureText: obscure,
        keyboardType: type,
        style: const TextStyle(color: Colors.white),
        decoration: InputDecoration(
          hintText: hint,
          hintStyle: const TextStyle(color: AppColors.muted),
          prefixIcon: Icon(icon, color: AppColors.accent, size: 20),
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

