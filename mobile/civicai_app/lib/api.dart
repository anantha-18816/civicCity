import 'dart:convert';

import 'package:http/http.dart' as http;

class ApiException implements Exception {
  final String message;
  ApiException(this.message);
  @override
  String toString() => message;
}

class Complaint {
  final int id;
  final int userId;
  final String issueType;
  final String title;
  final String? description;
  final double latitude;
  final double longitude;
  final String status;
  final int? priority;
  final int? duplicateOfId;
  final double? duplicateScore;
  final int? clusterId;
  final int? departmentId;
  final DateTime? createdAt;

  Complaint({
    required this.id,
    required this.userId,
    required this.issueType,
    required this.title,
    this.description,
    required this.latitude,
    required this.longitude,
    required this.status,
    this.priority,
    this.duplicateOfId,
    this.duplicateScore,
    this.clusterId,
    this.departmentId,
    this.createdAt,
  });

  factory Complaint.fromJson(Map<String, dynamic> j) => Complaint(
        id: j['id'] as int,
        userId: (j['userId'] ?? 0) as int,
        issueType: (j['issueType'] ?? 'UNKNOWN') as String,
        title: (j['title'] ?? '') as String,
        description: j['description'] as String?,
        latitude: (j['latitude'] as num).toDouble(),
        longitude: (j['longitude'] as num).toDouble(),
        status: (j['status'] ?? 'SUBMITTED') as String,
        priority: j['priority'] as int?,
        duplicateOfId: j['duplicateOfId'] as int?,
        duplicateScore: (j['duplicateScore'] as num?)?.toDouble(),
        clusterId: j['clusterId'] as int?,
        departmentId: j['departmentId'] as int?,
        createdAt:
            j['createdAt'] != null ? DateTime.tryParse(j['createdAt']) : null,
      );
}

class AiInsights {
  final Map<String, int> detectionsByObject;
  final Map<String, int> complaintsBySeverity;
  final double avgConfidence;
  final int duplicatesLinked;
  final int totalAnalyses;

  AiInsights({
    required this.detectionsByObject,
    required this.complaintsBySeverity,
    required this.avgConfidence,
    required this.duplicatesLinked,
    required this.totalAnalyses,
  });

  factory AiInsights.fromJson(Map<String, dynamic> j) => AiInsights(
        detectionsByObject: (j['detectionsByObject'] as Map<String, dynamic>?)
                ?.map((k, v) => MapEntry(k, (v as num).toInt())) ??
            {},
        complaintsBySeverity: (j['complaintsBySeverity'] as Map<String, dynamic>?)
                ?.map((k, v) => MapEntry(k, (v as num).toInt())) ??
            {},
        avgConfidence: (j['avgConfidence'] as num?)?.toDouble() ?? 0,
        duplicatesLinked: (j['duplicatesLinked'] as num?)?.toInt() ?? 0,
        totalAnalyses: (j['totalAnalyses'] as num?)?.toInt() ?? 0,
      );
}

class ApiClient {
  static const baseUrl = String.fromEnvironment(
    'API_BASE',
    defaultValue: 'http://localhost:8080/api',
  );

  static String? token;

  Map<String, String> get _headers => {
        'Content-Type': 'application/json',
        if (ApiClient.token != null) 'Authorization': 'Bearer ${ApiClient.token}',
      };

  Future<Map<String, dynamic>> login(String email, String password) async {
    final r = await http.post(Uri.parse('$baseUrl/auth/login'),
        headers: _headers,
        body: jsonEncode({'email': email, 'password': password}));
    if (r.statusCode != 200) throw ApiException('Login failed (${r.statusCode})');
    return jsonDecode(utf8.decode(r.bodyBytes)) as Map<String, dynamic>;
  }

  Future<Map<String, dynamic>> register(
      String name, String email, String password) async {
    final r = await http.post(Uri.parse('$baseUrl/auth/register'),
        headers: _headers,
        body:
            jsonEncode({'name': name, 'email': email, 'password': password}));
    if (r.statusCode != 201) throw ApiException('Registration failed (${r.statusCode})');
    return jsonDecode(utf8.decode(r.bodyBytes)) as Map<String, dynamic>;
  }

  Future<List<Complaint>> fetchComplaints() async {
    final r = await http.get(Uri.parse('$baseUrl/complaints'), headers: _headers);
    if (r.statusCode != 200) throw ApiException('Failed to load (${r.statusCode})');
    final list = jsonDecode(utf8.decode(r.bodyBytes)) as List<dynamic>;
    return list.map((e) => Complaint.fromJson(e)).toList();
  }

  Future<Complaint> createComplaint({
    required int userId,
    required String issueType,
    required String title,
    required String description,
    required double latitude,
    required double longitude,
  }) async {
    final r = await http.post(
      Uri.parse('$baseUrl/complaints'),
      headers: _headers,
      body: jsonEncode({
        'userId': userId,
        'issueType': issueType,
        'title': title,
        'description': description,
        'latitude': latitude,
        'longitude': longitude,
      }),
    );
    if (r.statusCode != 201) {
      throw ApiException('Submit failed (${r.statusCode}): ${r.body}');
    }
    return Complaint.fromJson(jsonDecode(utf8.decode(r.bodyBytes)));
  }

  Future<AiInsights> fetchInsights() async {
    final r =
        await http.get(Uri.parse('$baseUrl/dashboard/ai-insights'), headers: _headers);
    if (r.statusCode != 200) throw ApiException('Insights unavailable');
    return AiInsights.fromJson(jsonDecode(utf8.decode(r.bodyBytes)));
  }

  /// Connectivity probe used by the login screen.
  Future<bool> isBackendUp() async {
    try {
      final r = await http
          .get(Uri.parse('$baseUrl/health'))
          .timeout(const Duration(seconds: 4));
      return r.statusCode == 200;
    } catch (_) {
      return false;
    }
  }
}
