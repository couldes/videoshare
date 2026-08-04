const { chromium } = require('playwright');

const GATEWAY = 'http://localhost:7071';

async function main() {
  const browser = await chromium.launch();
  const context = await browser.newContext();
  const page = await context.newPage();

  let passed = 0;
  let failed = 0;

  function assert(condition, name) {
    if (condition) { passed++; console.log(`  [PASS] ${name}`); }
    else { failed++; console.log(`  [FAIL] ${name}`); }
  }

  // ============================================================
  // CC10+CC11: Gateway forwards same as direct
  // ============================================================
  console.log('\n=== CC10+CC11: Gateway forwards same as direct ===');

  const gwWeb = await page.request.get(`${GATEWAY}/web/account/checkCode`);
  const directWeb = await page.request.get('http://localhost:7070/account/checkCode');
  assert(gwWeb.status() === directWeb.status(),
    `Web: Gateway(${gwWeb.status()}) === Direct(${directWeb.status()})`);

  const gwAdmin = await page.request.get(`${GATEWAY}/admin/account/checkCode`);
  const directAdmin = await page.request.get('http://localhost:7075/admin/account/checkCode');
  assert(gwAdmin.status() === directAdmin.status(),
    `Admin: Gateway(${gwAdmin.status()}) === Direct(${gwAdmin.status()})`);

  // ============================================================
  // CC12: Admin path without token → 401 JSON
  // ============================================================
  console.log('\n=== CC12: Admin auth blocking ===');

  const noAuthResp = await page.request.get(`${GATEWAY}/admin/admin/video/audit`);
  assert(noAuthResp.status() === 401, 'Admin path without token → 401');
  const noAuthBody = await noAuthResp.json();
  assert(noAuthBody.status === 'error', 'Response has status=error');
  assert(noAuthBody.info.includes('登录'), 'Response mentions login');
  console.log(`  Body: ${JSON.stringify(noAuthBody)}`);

  // ============================================================
  // CC13: Admin login/checkCode exempt from auth
  // ============================================================
  console.log('\n=== CC13: Admin exempt paths ===');

  const loginResp = await page.request.post(`${GATEWAY}/admin/account/login`, {
    headers: { 'Content-Type': 'application/json' },
    data: { account: 'test', password: 'test', checkCodeKey: 'test' }
  });
  assert(loginResp.status() !== 401,
    `Admin login NOT blocked by Gateway (status ${loginResp.status()} !== 401)`);

  const checkCodeResp = await page.request.get(`${GATEWAY}/admin/account/checkCode`);
  assert(checkCodeResp.status() !== 401,
    `Admin checkCode NOT blocked by Gateway (status ${checkCodeResp.status()} !== 401)`);

  // ============================================================
  // CC14: innerApi blocked → 403
  // ============================================================
  console.log('\n=== CC14: innerApi blocking ===');

  const innerResp = await page.request.get(`${GATEWAY}/web/innerApi/video/list`);
  assert(innerResp.status() === 403, '/web/innerApi → 403');
  const innerBody = await innerResp.json();
  assert(innerBody.info.includes('内部接口'), 'Message mentions 内部接口');
  console.log(`  Body: ${JSON.stringify(innerBody)}`);

  // Also test innerApi inside admin route
  const innerAdminResp = await page.request.get(`${GATEWAY}/admin/innerApi/something`);
  assert(innerAdminResp.status() === 403, '/admin/innerApi → 403');

  // ============================================================
  // CC15: CORS preflight
  // ============================================================
  console.log('\n=== CC15: CORS preflight ===');

  const corsResp = await page.request.fetch(`${GATEWAY}/web/account/checkCode`, {
    method: 'OPTIONS',
    headers: {
      'Origin': 'http://localhost:3000',
      'Access-Control-Request-Method': 'GET',
      'Access-Control-Request-Headers': 'Authorization'
    }
  });
  assert(corsResp.status() === 200, `CORS preflight → ${corsResp.status()}`);
  assert(corsResp.headers()['access-control-allow-origin'] === 'http://localhost:3000',
    'Access-Control-Allow-Origin matches');
  assert(corsResp.headers()['access-control-allow-credentials'] === 'true',
    'Access-Control-Allow-Credentials is true');
  console.log(`  Allow-Origin: ${corsResp.headers()['access-control-allow-origin']}`);
  console.log(`  Allow-Methods: ${corsResp.headers()['access-control-allow-methods']}`);
  console.log(`  Allow-Credentials: ${corsResp.headers()['access-control-allow-credentials']}`);

  // ============================================================
  // CC19: User login flow (verify checkCode image available)
  // ============================================================
  console.log('\n=== CC19: User flow ===');

  const ccResp = await page.request.get(`${GATEWAY}/web/account/checkCode`);
  // checkCode returns image/captcha, status should be 200 but may need params
  assert(ccResp.status() < 500,
    `CheckCode endpoint accessible (status ${ccResp.status()})`);

  // ============================================================
  // Summary
  // ============================================================
  console.log(`\n========================================`);
  console.log(`Results: ${passed} passed, ${failed} failed`);
  console.log(`========================================`);

  await browser.close();
  process.exit(failed > 0 ? 1 : 0);
}

main().catch(err => {
  console.error('Test error:', err);
  process.exit(1);
});
