import 'package:civicai_app/main.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('app builds', (tester) async {
    await tester.pumpWidget(const CivicAiApp());
    expect(find.byType(CivicAiApp), findsOneWidget);
  });
}
