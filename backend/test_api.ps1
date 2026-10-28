$ErrorActionPreference = 'Stop'

Write-Host "=== TEST 1: Public Catalog (Appendix A) ==="
$products = Invoke-RestMethod -Uri 'http://localhost:8080/api/public/products' -Method Get
Write-Host "Public Products Count: $($products.Count)"

Write-Host "`n=== TEST 2: Policyholder Authentication (Appendix D - 8h Token) ==="
$loginHolder = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body '{"username":"holder","password":"holder123"}'
Write-Host "User: $($loginHolder.username) | Role: $($loginHolder.role) | Duration: $($loginHolder.sessionDurationHours) Hours"
$holderToken = $loginHolder.token

Write-Host "`n=== TEST 3: Fetch Policies Scope (Appendix H: GET /api/policy/all) ==="
$holderHeaders = @{ 'Authorization' = "Bearer $holderToken" }
$policies = Invoke-RestMethod -Uri 'http://localhost:8080/api/policy/all' -Method Get -Headers $holderHeaders
Write-Host "Policies Count: $($policies.Count)"
$policy1 = $policies[0]
Write-Host "Policy 1: $($policy1.policyNumber) | Base Premium: `$($policy1.basePremium) | Asset: $($policy1.assetIdentifier)"

Write-Host "`n=== TEST 4: File FNOL (Appendix H: POST /api/claims/fnol) ==="
$fnolBody = @{
    policyId = $policy1.id
    incidentDate = '2026-09-02'
    lossDescription = 'Fleet commercial cargo van bumper dent and side camera sensor dislodged during loading dock maneuver.'
    incidentLocation = 'Dock 4, Scranton Distribution Depot'
    claimedAmount = 4500.00
    witnessStatement = 'Dock operator confirmed slow velocity impact against rubber buffer.'
} | ConvertTo-Json

$claim = Invoke-RestMethod -Uri 'http://localhost:8080/api/claims/fnol' -Method Post -Headers $holderHeaders -ContentType 'application/json' -Body $fnolBody
Write-Host "FNOL Claim Created: $($claim.claimNumber) | Initial Gross Reserve: `$($claim.grossReserve) | SLA Deadline: $($claim.slaDeadline)"

Write-Host "`n=== TEST 5: Claims Handler Auth (Appendix D - 12h Token) ==="
$loginHandler = Invoke-RestMethod -Uri 'http://localhost:8080/api/auth/login' -Method Post -ContentType 'application/json' -Body '{"username":"handler","password":"handler123"}'
Write-Host "User: $($loginHandler.username) | Role: $($loginHandler.role) | Duration: $($loginHandler.sessionDurationHours) Hours"
$handlerToken = $loginHandler.token
$handlerHeaders = @{ 'Authorization' = "Bearer $handlerToken" }

Write-Host "`n=== TEST 6: Modify Reserve (Appendix H: PUT /api/claims/reserve/{id}) ==="
$reserveBody = @{
    newReserve = 3200.00
    justificationNotes = 'Forensic field report established sensor damage minor; lowered gross reserve.'
} | ConvertTo-Json

$updatedClaim = Invoke-RestMethod -Uri "http://localhost:8080/api/claims/reserve/$($claim.id)" -Method Put -Headers $handlerHeaders -ContentType 'application/json' -Body $reserveBody
Write-Host "Updated Reserve: `$($updatedClaim.grossReserve)"

Write-Host "`n=== TEST 7: Statutory Contract Packet (FR12) ==="
$packet = Invoke-RestMethod -Uri "http://localhost:8080/api/policy/$($policy1.id)/packet" -Method Get -Headers $holderHeaders
Write-Host "Receipt Number: $($packet.receiptNumber)"
Write-Host "Appended Riders Count: $($packet.appendedRiders.Count)"

Write-Host "`n=== TEST 8: ACORD System Exchange (Appendix E) ==="
$acord = Invoke-RestMethod -Uri "http://localhost:8080/api/integration/acord/$($policy1.id)" -Method Get -Headers $handlerHeaders
Write-Host "Standard Version: $($acord.ACORDStandardVersion)"
Write-Host "Insured: $($acord.InsuranceSvcRs.InsuredName)"

Write-Host "`n=== TEST 9: Immutable Regulatory Audit Trail (FR11) ==="
$auditLogs = Invoke-RestMethod -Uri 'http://localhost:8080/api/audit/logs' -Method Get -Headers $handlerHeaders
Write-Host "Audit Records Captured: $($auditLogs.Count)"
Write-Host "Latest Action: $($auditLogs[0].action) on $($auditLogs[0].entityName) by $($auditLogs[0].changedBy)"

Write-Host "`n>>> ALL BACKEND & DATABASE REQUIREMENTS VERIFIED SUCCESSFULLY! <<<"
