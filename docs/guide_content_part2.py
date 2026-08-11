"""
Second half of the ZeroFake guide: design decisions, security, limitations,
the interview question bank, and the cheat sheet.
"""

EXTRA = []
A = EXTRA.append

# ===========================================================================
# PART V
# ===========================================================================

A(("part", "Part V — Design Decisions and Trade-offs"))

A(("p",
   "This is the part of the project that distinguishes engineering from assembly. Every "
   "decision below was a genuine fork in the road. For each one: what was chosen, why, "
   "what it costs, and what would change the answer."))

A(("h1", "5.1  How the other services validate tokens"))
A(("p", "**Options:** a shared-secret validation filter duplicated in each service; a shared library; or an API gateway that validates centrally and forwards a trusted header."))
A(("p", "**Chosen:** a validation filter in each service, all sharing one secret."))
A(("p",
   "**Why:** the gateway approach centralises validation but introduces a single point "
   "of failure and a component that must be running before anything works. A shared "
   "library removes duplication but couples all four services to one artifact's release "
   "cycle &mdash; upgrading a security dependency then means coordinating four releases. "
   "Duplicating roughly eighty lines of filter code keeps the services genuinely "
   "independent."))
A(("p",
   "**Cost:** the same code exists four times, so a bug must be fixed four times. **What "
   "would change it:** more services, or services owned by different teams &mdash; at "
   "which point a gateway starts paying for itself."))

A(("h1", "5.2  Putting identity claims in the token"))
A(("p", "**Chosen:** the JWT carries `userId` and `role`."))
A(("p",
   "**Why:** the alternative is that every service calls the authentication service on "
   "every request to resolve the caller. That makes auth a hard synchronous dependency "
   "of the entire platform: if it is slow, everything is slow; if it is down, everything "
   "is down."))
A(("p",
   "**Cost, and this is the important half of the answer:** claims are a *snapshot* taken "
   "at login. If an administrator revokes a manufacturer's role, that user keeps the old "
   "role until their access token expires &mdash; up to 15 minutes. This is the classic "
   "stateless-token trade-off. The 15-minute expiry is the mitigation, and it is a "
   "deliberate bound on the staleness window. If immediate revocation were a requirement, "
   "the options would be a shorter expiry, a revocation list checked per request, or "
   "accepting the coupling of a lookup."))

A(("h1", "5.3  Who updates the product's blockchain status"))
A(("p", "**Options:** the browser makes a second call after registering; the blockchain service calls the product service; or the product service drives the whole flow."))
A(("p", "**Chosen:** the blockchain service notifies the product service."))
A(("p",
   "**Why:** the browser must not own a cross-service workflow. If the user closes the "
   "tab between the two calls, the product is on the ledger but the catalogue still says "
   "`PENDING` &mdash; a permanent inconsistency caused by a UI event. Business workflows "
   "belong on the server."))
A(("p",
   "**Cost:** it creates a dependency from blockchain to product, which is a direction "
   "worth justifying. The justification is that the ledger write is the authoritative "
   "event, so the service that performs it is the one that knows the outcome. The call "
   "is best-effort and non-fatal, because the ledger write cannot be rolled back."))

A(("h1", "5.4  Which enum is canonical for product blockchain status"))
A(("p",
   "**Problem:** the product service used `PENDING / REGISTERED / SUCCESS / FAILED`, the "
   "blockchain service used `PENDING / SUCCESS / FAILED`, and the front end recognised a "
   "third combination. `SUCCESS` had been added to the product enum later, which broke a "
   "database check constraint, and the workaround was a start-up task that executed raw "
   "SQL to drop the constraint on every boot."))
A(("p", "**Chosen:** the two enums describe genuinely different things and should not match."))
A(("ul", [
    "A **product** has a lifecycle: `PENDING` &rarr; `REGISTERED`, or `FAILED`.",
    "A **ledger transaction** has an outcome: `PENDING` &rarr; `SUCCESS`, or `FAILED`.",
]))
A(("p",
   "Forcing one enum on both would conflate a durable state with the result of a single "
   "operation. With the product enum corrected, the constraint-dropping start-up hack "
   "was deleted entirely. **The lesson worth articulating:** a workaround that runs on "
   "every start-up is usually a symptom of a modelling error, not a deployment problem."))

A(("h1", "5.5  Whether verification transfers ownership"))
A(("p",
   "**Original behaviour:** when a customer verified a genuine product, the system "
   "automatically transferred on-chain ownership to them &mdash; and the logic existed in "
   "*both* the front end and the fraud service, so both fired on the same scan and the "
   "second always failed."))
A(("p", "**Chosen:** removed from both. Verification is strictly read-only."))
A(("p", "**Why**, in order of severity:"))
A(("ol", [
    "**It is a security hole.** Ownership would transfer to whoever scanned the code. Anyone photographing a QR code in a shop could take on-chain ownership of stock they had not bought.",
    "**It breaks the service boundary.** Fraud detection is an *observer*. Giving it the power to write custody events to the ledger makes it an actor in the supply chain.",
    "**It corrupts the evidence.** The fraud rules reason about scan history. If scanning also mutates ownership, the act of investigating changes the thing being investigated.",
    "**Inspection is not custody.** Picking an item up in a shop does not make it yours. The digital model should not claim otherwise.",
]))
A(("key", "This is the strongest single answer in the project",
   "If asked \"what is the most important design decision you made?\", this is the one to "
   "reach for. It has a clear security argument, a clear architectural argument, and a "
   "clear real-world analogy &mdash; and it demonstrates that you removed a feature because "
   "it was wrong, which is harder and more valuable than adding one."))

A(("h1", "5.6  QR code generation"))
A(("p",
   "**Chosen:** generated in the product service, on creation, encoding only the product "
   "UUID, stored as a PNG served through an authenticated endpoint."))
A(("p",
   "**Why the product service:** the QR code is catalogue metadata about a product, and "
   "the product service owns products. Putting it in the blockchain service would mean "
   "that service knowing about presentation concerns."))
A(("p",
   "**Why not encode more data:** covered in section 4.7 &mdash; a richer payload invites "
   "clients to trust the label. **The one real weakness:** images are written to the "
   "container filesystem via a mounted volume. In a multi-instance deployment they would "
   "need shared object storage such as S3, or regeneration on demand, which is cheap "
   "since the input is just the UUID."))

A(("h1", "5.7  Fabric CA and wallet management"))
A(("p",
   "**Chosen:** a static X.509 identity read from the network's crypto material, rather "
   "than certificate enrolment through Fabric CA with a managed wallet."))
A(("p",
   "**Why:** the application connects with one Org1 identity, and the network is "
   "a demonstration network rather than a production one. Full CA enrolment "
   "&mdash; registering identities, enrolling them, storing credentials in a wallet, "
   "handling renewal and revocation &mdash; is several days of work that would "
   "demonstrate nothing this project does not already demonstrate."))
A(("p",
   "**Cost, stated plainly:** it does not scale to multiple organisations, offers no "
   "credential rotation, and would be unacceptable in production. **This is documented "
   "in the README rather than hidden**, which is the right handling for a deliberate "
   "scope decision."))

A(("h1", "5.8  Recording ledger transactions honestly"))
A(("p",
   "**Original behaviour:** when registration failed because the product already existed "
   "on-chain, the code searched the *rendered stack trace* for the string \"already "
   "exists\", and on finding it wrote an audit record with a **fabricated** transaction "
   "identifier (`ALREADY_REGISTERED_<uuid>`) and status `SUCCESS`."))
A(("p", "**Chosen:** never synthesise ledger data. Specifically:"))
A(("ul", [
    "The transaction ID is read from the **proposal**, so it is real and is known even when submission fails.",
    "The block number is read from the **commit status**, so it is real.",
    "A failure is recorded as `FAILED` with the chaincode's own error message.",
    "`blockHash` stays null, because the Gateway client does not expose it &mdash; a null is honest, a placeholder is a lie.",
    "Error classification reads the **structured gRPC error details**, not a stack trace.",
]))
A(("warn", "Why this matters more here than anywhere else",
   "The entire value proposition of this project is that the audit trail can be trusted. "
   "An audit trail containing identifiers that exist nowhere on the ledger is worse than "
   "no audit trail, because it looks authoritative. If an examiner finds this kind of "
   "code, the whole project's premise collapses &mdash; so being able to say \"we record "
   "only what the ledger actually told us\" is essential."))

A(("h1", "5.9  The oracle problem &mdash; the limitation to raise yourself"))
A(("p",
   "A blockchain guarantees that **the digital record** was not altered. It guarantees "
   "nothing about whether the digital record ever matched physical reality. If a "
   "manufacturer registers a product that was never made, or a genuine QR label is peeled "
   "off and stuck on a fake, the ledger records the lie perfectly and permanently."))
A(("p", "**This is the oracle problem, and no blockchain solves it.** What ZeroFake does about it:"))
A(("ul", [
    "**Narrow the gap.** Only a manufacturer role can register products, so the input is at least from an accountable party.",
    "**Detect the consequences.** A relabelled fake produces impossible scan histories, which is what `DUPLICATE_QR` and `MULTIPLE_LOCATION_SCAN` exist to catch.",
    "**Accept the residue.** The remaining gap is closed with physical measures &mdash; tamper-evident packaging, NFC chips, holographic seals &mdash; not with software.",
]))
A(("tip", "How to deploy this in the viva",
   "Raise it yourself, in these words: *\"The honest limitation is the oracle problem: the "
   "blockchain proves the record wasn't tampered with, not that the record was ever true. "
   "We narrow the gap by restricting who can register and by detecting the physical "
   "impossibilities a cloned label creates, but closing it completely needs hardware, not "
   "software.\"* An examiner who was about to test you on this will instead conclude you "
   "have thought harder about the problem than the average candidate."))

A(("h1", "5.10  What was deliberately not built"))
A(("table", [
    ["Not built", "Reasoning"],
    ["**Kubernetes**", "Four services on one host. Compose reproduces the stack in one command; Kubernetes would add a control plane, manifests and an operational burden with no benefit at this scale."],
    ["**Message broker**", "Verification is synchronous by nature &mdash; the user is waiting for an answer. A queue would add latency and complexity to solve a problem that does not exist here."],
    ["**Redis / caching**", "No measured performance problem. Caching added speculatively buys invalidation bugs and nothing else."],
    ["**Service discovery**", "Compose provides DNS by service name. Eureka or Consul would be infrastructure for its own sake."],
    ["**Circuit breaker**", "A defensible gap. With more services or flakier dependencies, Resilience4j on the Feign clients would be the first thing to add."],
], [22, 78]))
A(("key", "Deliberate omission is a design skill",
   "Being able to say *why you did not use* a technology is often more impressive than "
   "listing what you did use. It shows you evaluate tools against a problem rather than "
   "collecting them."))

# ===========================================================================
# PART VI
# ===========================================================================

A(("h1", "5.11  Serving everything from one origin"))
A(("p",
   "**Problem:** in development the browser calls four services on four ports, "
   "which means four origins and therefore CORS on every request."))
A(("p",
   "**Chosen:** in the deployed configuration, nginx terminates TLS and routes path "
   "prefixes &mdash; `/auth/`, `/products/`, `/blockchain/`, `/fraud/` &mdash; to the "
   "services behind it. The browser sees a single origin."))
A(("p", "Three things follow, and they are worth listing in this order:"))
A(("ul", [
    "**One port faces the internet instead of five.** Neither the database nor any service port is published at all.",
    "**Cross-origin requests stop existing.** CORS is not configured more carefully; it becomes irrelevant, which is a better outcome than getting the headers right.",
    "**TLS terminates in exactly one place**, so certificate renewal is one concern rather than five.",
]))
A(("p",
   "The client is built with *relative* API paths rather than absolute URLs, so it "
   "inherits whatever origin serves it. Changing the domain needs no rebuild of the "
   "client &mdash; only the CORS configuration, which exists for non-browser callers."))
A(("key", "The general principle",
   "The best way to handle a class of problem is often to arrange things so the problem "
   "cannot arise. CORS exists to police cross-origin requests; a single-origin topology "
   "means there are none to police."))

A(("h1", "5.12  Verifying by running, not by reasoning"))
A(("p",
   "Every service compiled. Ninety-six unit tests passed. The code had been reviewed "
   "line by line. None of that established that the system worked, and running it "
   "against a real Fabric network proved the point immediately by exposing two faults "
   "that no amount of compiling or unit testing could have found."))
A(("h2", "The service could not read its own credentials"))
A(("p",
   "Fabric's crypto material is `0700` directories with a `0600` private key, which is "
   "exactly right for signing material. The service images run as a non-root user with "
   "a different uid, which is exactly right for container hardening. Both decisions were "
   "correct, and together they meant the blockchain service could not open its own "
   "identity."))
A(("p",
   "The fix was to run that container **as the owner of the credentials it is given**, "
   "rather than loosening permissions on a private key. Worth noticing: this was not a "
   "bug in either decision. It was a bug in the space between them, which is where "
   "integration faults usually live."))
A(("h2", "The status update silently failed"))
A(("p",
   "Registration committed to the ledger, but the product was never promoted to "
   "`REGISTERED`. The cause was `Invalid HTTP method: PATCH` &mdash; `HttpURLConnection` "
   "has never supported PATCH, and it backed the HTTP client that had been chosen "
   "earlier to work around an unrelated JDK problem. Apache HttpClient has neither "
   "limitation."))
A(("p",
   "The instructive part is what happened while it was broken. The call is deliberately "
   "best-effort and non-fatal, because a ledger write cannot be rolled back &mdash; so the "
   "transaction committed, the failure was logged, and the user got a successful "
   "response with a catalogue status that was merely stale. **The system degraded "
   "exactly as designed while carrying a real bug.** That is what designing for failure "
   "buys you."))
A(("tip", "A strong answer to \"what did you learn?\"",
   "*\"That compiling and passing unit tests tells you your logic is right, not that your "
   "system works. Both bugs I found by running it were in the seams &mdash; file permissions "
   "between the host and a container, and an HTTP verb an old client does not support. "
   "Neither is visible from inside a unit test, because a unit test mocks away exactly "
   "the things that broke.\"* This is a genuinely senior observation and very few "
   "candidates make it."))

A(("part", "Part VI — Security Analysis"))

A(("h1", "6.1  Threat model"))
A(("table", [
    ["Threat", "Mitigation"],
    ["Counterfeit product with a fabricated code", "No catalogue record &rarr; `PRODUCT_NOT_FOUND`, risk 100"],
    ["Counterfeit with a copied genuine code", "Impossible scan history &rarr; `DUPLICATE_QR` + `MULTIPLE_LOCATION_SCAN`"],
    ["Attacker registers products on the ledger", "`register-product` requires `ROLE_MANUFACTURER` or `ROLE_ADMIN`"],
    ["Attacker steals ownership of stock", "Chaincode verifies the claimed current owner really is the owner; verification never transfers"],
    ["Privilege escalation at sign-up", "Self-registration is restricted to `ROLE_CUSTOMER`; privileged roles need an admin token"],
    ["Forged scan attribution", "Scanner identity is taken from the signed token, never from the request body"],
    ["Token forgery", "HMAC-SHA256 signature plus a required issuer claim"],
    ["Password database leak", "BCrypt with a per-password salt and a deliberate work factor"],
    ["Information disclosure via errors", "Generic client messages; full detail logged server-side only"],
    ["Enumerating registered users", "Login returns an identical 401 for unknown email and wrong password"],
    ["Path traversal on QR download", "The resolved path is checked to remain inside the storage directory"],
    ["Tampering with the audit trail", "Ledger data is written by Fabric; the local trail records only what the ledger returned"],
], [34, 66]))

A(("h1", "6.2  Honest security gaps"))
A(("p", "You should know these before an examiner finds them:"))
A(("ul", [
    "**Tokens are stored in `localStorage`**, which is readable by any JavaScript on the page, so a cross-site scripting flaw would expose them. The alternative &mdash; an `HttpOnly` cookie &mdash; resists XSS but requires CSRF protection. Neither is free; the cookie approach is the stronger default and would be the production choice.",
    "**One shared HMAC secret** means any service could forge a token, not merely verify one. RS256 with per-service public keys removes that.",
    "**No rate limiting.** Login is brute-forceable at the network level. BCrypt's cost makes this slow, but a real deployment needs per-IP throttling and account lockout.",
    "**No HTTPS in local deployment.** Tokens travel in clear over HTTP on localhost. Any real deployment terminates TLS at a reverse proxy.",
    "**Role changes take up to 15 minutes to take effect**, as discussed in 5.2.",
    "**No audit log of administrative actions.** Who created which product, and who changed a role, is not separately recorded.",
]))
A(("tip", "Naming your own gaps is a strength",
   "There is no such thing as a system with no security gaps &mdash; only systems whose "
   "owners have not looked. An examiner asking \"what are the security weaknesses?\" is "
   "testing whether you can assess your own work. A candidate who lists six specific, "
   "accurate gaps with the trade-off behind each is more convincing than one who claims "
   "there are none."))

# ===========================================================================
# PART VII
# ===========================================================================

A(("part", "Part VII — Limitations and Future Work"))

A(("h1", "7.1  What the system does not do"))
A(("ul", [
    "**It cannot verify physical authenticity** &mdash; the oracle problem of section 5.9.",
    "**The two organisations are operated by one person on one machine.** The network does run Org1 and Org2, and the chaincode inherits the channel's `MAJORITY Endorsement` policy, so both must endorse every transaction &mdash; the mechanism is genuinely there. What is missing is *independence*: both organisations share one administrator and one host, so the trust distribution is demonstrated rather than real. Production would put the manufacturer, distributors and retailers in separately operated organisations.",
    "**`blockHash` is never populated**, because the Gateway client does not expose it. Capturing it requires a block event listener.",
    "**Fabric CA is not integrated**, per section 5.7.",
    "**No pagination.** Listing endpoints return every row, which will not survive a realistic catalogue.",
    "**The multi-service workflow is only covered by a script that must be run by hand.** `scripts/smoke-test.sh` exercises the running stack over HTTP, but nothing runs it automatically. A continuous integration pipeline that stood the stack up and ran it on every push would catch cross-service regressions that unit tests cannot see.",
]))

A(("h1", "7.2  What to build next, in order"))
A(("ol", [
    "**Independently operated organisations.** Two organisations already endorse every transaction; the remaining gap is that one person administers both. Separating them is the highest-value change, because independence is what converts a demonstrated mechanism into a real trust guarantee.",
    "**Continuous integration** that builds the images, stands up the stack and runs the smoke script on every push.",
    "**Pagination and filtering** on all list endpoints.",
    "**Flyway migrations** replacing `ddl-auto`.",
    "**Rate limiting and account lockout** on the authentication endpoints.",
    "**Circuit breakers** on the Feign clients.",
    "**A block event listener** to populate block hashes and to detect ledger events the application did not initiate.",
]))

# ===========================================================================
# PART VIII
# ===========================================================================

A(("part", "Part VIII — Interview Question Bank"))

A(("p",
   "Model answers below are deliberately concise &mdash; the shape of a good spoken answer, "
   "not an essay. Say the short version first, then expand if invited."))

A(("h1", "8.1  Warm-up"))

A(("q", "Q. Explain your project in two minutes."))
A(("p", "Use the pitch from section 1.3. Problem &rarr; the three checks &rarr; the architecture &rarr; one honest limitation."))

A(("q", "Q. Why did you choose this problem?"))
A(("p",
   "\"Counterfeiting causes real harm in categories like medicines and automotive parts, "
   "and the root cause is structural: a physical label cannot prove its own origin. That "
   "makes it a genuinely interesting distributed-systems problem rather than a CRUD "
   "application with a blockchain bolted on.\""))

A(("q", "Q. What was the hardest part?"))
A(("p",
   "Pick something real and specific. A strong answer: \"Getting the failure semantics "
   "right. It took me a while to realise that 'the blockchain service is unreachable' "
   "and 'this product is not on the blockchain' had been collapsed into the same error "
   "path, which meant the system returned a 500 at exactly the moment it should have "
   "reported a counterfeit. Separating 'we could not check' from 'we checked and it is "
   "fake' changed how I handle errors across the whole project.\""))

A(("q", "Q. What would you do differently?"))
A(("p",
   "\"I would design the error taxonomy first rather than retrofitting it, I would use "
   "Flyway from day one instead of `ddl-auto`, and I would start with a two-organisation "
   "Fabric network, because a single-org network undermines the trust argument for using "
   "a blockchain at all.\""))

A(("h1", "8.2  Architecture"))

A(("q", "Q. Why microservices instead of a monolith?"))
A(("p", "Give the four reasons in section 2.2 &mdash; then volunteer the counter-argument and concede it. Keep the blockchain-service boundary as the one you would defend regardless."))

A(("q", "Q. How do services communicate, and why not a message queue?"))
A(("p",
   "\"Synchronous HTTP through Feign clients. A queue would be wrong here: verification is "
   "request-response, the user is waiting for a verdict, and asynchrony would add latency "
   "and complexity without solving anything. If I added notifications or batch "
   "re-verification, that work would be a natural fit for a queue.\""))

A(("q", "Q. How does one service authenticate to another?"))
A(("p",
   "\"It forwards the caller's own bearer token rather than using a service account. No "
   "service holds an ambient privilege, and downstream services apply exactly the same "
   "authorisation they would for a direct call. The trade-off is that a compromised "
   "service can act as the user, and there is no way to express 'the fraud service may "
   "read products' as distinct from 'this user may read products'.\""))

A(("q", "Q. What happens if the blockchain service goes down?"))
A(("p",
   "\"Verification returns 502 with 'authenticity cannot be confirmed'. It deliberately "
   "does not return a counterfeit verdict, because a dependency outage is not evidence "
   "about the product. Product management and authentication are unaffected, which is "
   "one of the reasons that service is isolated.\""))

A(("q", "Q. How do you handle distributed transactions?"))
A(("p",
   "\"I avoid needing them. The only cross-service write is registering on the ledger and "
   "then marking the product `REGISTERED`, and those cannot be atomic &mdash; a blockchain "
   "transaction cannot be rolled back. So I made the ledger authoritative and the "
   "catalogue status a best-effort projection that can be repaired. If I needed real "
   "distributed consistency I would use the saga pattern with compensating actions, but "
   "here the honest answer is that one side simply cannot be compensated.\""))

A(("h1", "8.3  Security"))

A(("q", "Q. Walk me through what happens when a user logs in."))
A(("p", "Section 3.1, then the token payload. Emphasise the two custom claims and why they exist."))

A(("q", "Q. Is a JWT encrypted?"))
A(("p",
   "\"No. It is Base64URL-encoded, which is trivially reversible &mdash; anyone with the "
   "token can read every claim. The signature guarantees integrity, not confidentiality. "
   "That is why nothing sensitive goes in the payload; I have a unit test asserting the "
   "password hash never appears in a token. If the payload had to be confidential, that "
   "is JWE rather than JWS.\""))

A(("q", "Q. How would you revoke a JWT?"))
A(("p",
   "\"You cannot revoke a stateless token directly &mdash; nothing is consulted when it is "
   "verified. I handle it with the two-token split: access tokens live 15 minutes so the "
   "damage window is bounded, and refresh tokens are stored in the database so logout "
   "deletes them and they stop working immediately. For instant revocation of access "
   "tokens you would need a denylist checked on every request, which trades away the "
   "statelessness you chose JWTs for in the first place.\""))

A(("q", "Q. Why BCrypt rather than SHA-256?"))
A(("p",
   "\"Because SHA-256 is fast, and speed helps the attacker. BCrypt is deliberately slow "
   "with a tunable work factor, and it salts each password automatically so identical "
   "passwords produce different hashes and rainbow tables are useless. Argon2 is the "
   "more modern choice and is what I would use now.\""))

A(("q", "Q. Someone steals a manufacturer's token. What can they do?"))
A(("p",
   "\"Anything that manufacturer can do, for up to 15 minutes: create products and register "
   "them on the ledger. They cannot escalate to admin, because the role is baked into "
   "the signed token. Mitigations I have: short expiry and refresh-token revocation. "
   "Mitigations I do not have: token binding to a device or IP, and immediate revocation "
   "&mdash; I would add both before a real deployment.\""))

A(("q", "Q. Why can't users register as an admin?"))
A(("p",
   "\"Because a manufacturer or admin account is a position of trust in the supply chain "
   "&mdash; a manufacturer can mint product identities on the blockchain. Those roles must "
   "be granted by an existing administrator, not claimed. Public registration creates "
   "customers only, and requesting anything else without an admin token is a 400.\""))

A(("h1", "8.4  Blockchain &mdash; expect the hardest questions here"))

A(("q", "Q. What does the blockchain actually give you that a PostgreSQL table with an audit log would not?"))
A(("p", "**This is the single most important question in the viva.** A weak answer sinks the project. The strong answer has three parts:"))
A(("ol", [
    "**\"With a database, the party that owns the database can rewrite history.\"** If the manufacturer hosts the audit log, then the manufacturer can silently alter it, and a distributor in a dispute has no reason to accept it as evidence. The ledger is replicated across organisations, so no single participant can rewrite it unilaterally.",
    "**\"Validation is enforced by every participant, not by my application server.\"** Ownership transfer rules live in chaincode that every endorsing peer re-executes independently. A compromised or buggy application server cannot write an invalid transfer, because the peers would reject it. In a database architecture, whoever controls the application controls the data.",
    "**\"And I will be honest about the caveat.\"** My network runs two organisations and the chaincode uses the channel's `MAJORITY Endorsement` policy, so both Org1 and Org2 must endorse every transaction &mdash; that second argument holds mechanically. What it does not yet have is *independence*: I administer both organisations on one machine, so nobody is actually checking my work. Separating the operators is what turns the demonstration into a guarantee, and it is a configuration change rather than an architectural one.",
]))
A(("tip", "Why the concession makes the answer stronger",
   "Almost every student answers this question with \"immutability\" and stops. Naming the "
   "specific property (no unilateral rewrite; distributed validation) *and* the specific "
   "condition under which your own deployment does not yet fully deliver it is what "
   "separates understanding from recitation."))

A(("q", "Q. Why Hyperledger Fabric and not Ethereum?"))
A(("p", "Section 4.4: known participants, confidentiality of commercial data, no per-transaction fees, throughput, no cryptocurrency."))

A(("q", "Q. Explain the Fabric transaction lifecycle."))
A(("p", "Propose &rarr; endorse (simulate, produce a read-write set) &rarr; order into a block &rarr; validate (endorsement policy and read-set versions) &rarr; commit. Mention that invalid transactions are still recorded, marked invalid."))

A(("q", "Q. What is the difference between the world state and the ledger?"))
A(("p",
   "\"The ledger is the immutable chain of blocks &mdash; the full history. The world state "
   "is a key-value database holding only the current value of each key, and it is "
   "derivable by replaying the chain. Queries hit the world state because it is fast; "
   "`GetProductHistory` walks the actual chain, which is where the audit trail comes "
   "from.\""))

A(("q", "Q. Why must chaincode be deterministic?"))
A(("p",
   "\"Every endorsing peer executes it independently and their read-write sets must match "
   "exactly, or endorsement fails. So no `time.Now()`, no randomness, no network calls. "
   "My chaincode uses the transaction timestamp from the stub, which is identical on "
   "every peer.\""))

A(("q", "Q. Your chaincode sets `isVerified` to true at registration and never changes it. So what does `VerifyProduct` actually verify?"))
A(("p", "**A sharp question. Do not bluff.**"))
A(("p",
   "\"It verifies existence and returns the current on-chain state &mdash; owner, role, "
   "status, timestamps. The `isVerified` flag is effectively vestigial: in the current "
   "model, having a ledger identity *is* the authenticity claim, so a separate boolean "
   "adds nothing. The real verification work happens above it, in the fraud engine, which "
   "compares the on-chain owner and the scan history against what is physically "
   "plausible. If I were tightening the model I would either remove the flag or give it "
   "meaning &mdash; for example clearing it when a product is reported stolen or recalled, "
   "which is a genuinely useful feature.\""))

A(("q", "Q. Is your network really decentralised?"))
A(("p",
   "\"Partly. It runs two organisations, and the chaincode inherits the channel's "
   "`MAJORITY Endorsement` policy, so Org1 and Org2 both have to endorse every "
   "transaction &mdash; I can show you the commit output, both approvals are there. "
   "So the endorsement mechanism is real. What is not real is independence: I run "
   "both organisations on one machine, so a dishonest me could still rewrite "
   "everything. Genuine decentralisation needs separate operators, which is a "
   "deployment change rather than a code change &mdash; the chaincode and gateway code "
   "are already multi-organisation.\""))

A(("q", "Q. What is an MVCC conflict in Fabric?"))
A(("p",
   "\"Endorsement simulates against a snapshot of the world state and records the version "
   "of every key it read. If another transaction changes one of those keys before this "
   "one is validated, the versions no longer match and the transaction is rejected at "
   "validation. It is optimistic concurrency control; the client retries. It matters here "
   "because two rapid ownership transfers of the same product would conflict.\""))

A(("q", "Q. How did you test the chaincode?"))
A(("p",
   "\"23 tests at 82.6% statement coverage, with hand-written fakes for the Fabric stub "
   "rather than generated mocks &mdash; the contract only touches four stub methods, so a "
   "fake that embeds the interface and implements those four is smaller than the "
   "generated alternative. Embedding matters: any method the contract calls that the "
   "fake does not define panics, instead of quietly returning a zero value and letting "
   "an untested path pass.\""))
A(("p",
   "\"Two of those tests assert *error wording*, which sounds odd until you see why. "
   "The chaincode saying `does not exist` is what the blockchain service matches to "
   "return a 404, which the fraud service reads as `BLOCKCHAIN_MISMATCH`. If someone "
   "reworded that message, a counterfeit would silently start surfacing as a 502 "
   "'cannot verify' instead of a verdict. Nothing else in the codebase would have "
   "caught it, so the phrase is a contract between components rather than a message.\""))

A(("q", "Q. Have you actually run this end to end, or just tested the parts?"))
A(("p",
   "\"Both, and running it mattered. 41 assertions pass against the running stack with a "
   "live Fabric network &mdash; register on the ledger, confirm the transaction ID and "
   "block number are real, verify as genuine, read the history back. Doing that found "
   "two bugs that ninety-six passing unit tests had not: the container could not read "
   "its own Fabric credentials because of a file-permission mismatch, and a status "
   "update was failing because HttpURLConnection rejects PATCH. Both were in the seams "
   "between components, which is precisely what unit tests mock away.\""))

A(("q", "Q. How would you deploy this?"))
A(("p",
   "\"One VM running everything, because Fabric needs a peer somewhere and no mainstream "
   "platform-as-a-service will host one. nginx terminates TLS and routes path prefixes "
   "to the four services, so the browser talks to a single origin: one port exposed "
   "instead of five, no cross-origin requests at all, and TLS in one place. The database "
   "and service ports are not published. It needs about 8 GB of RAM &mdash; four JVMs plus "
   "the Fabric network &mdash; and the scripts to provision it are in the repository.\""))

A(("q", "Q. What would you do if the blockchain became a bottleneck?"))
A(("p",
   "\"Registration is the slow path, because it waits for a block to commit &mdash; a second "
   "or two. The fix is already half-built: a product starts as `PENDING` and is promoted "
   "to `REGISTERED` when the transaction commits, so making registration asynchronous is "
   "a matter of returning immediately and letting a listener do the promotion. That "
   "status field exists for exactly this reason. Reads are not a concern: verification "
   "queries the world state through one peer and never touches the ordering service.\""))

A(("h1", "8.5  Data and persistence"))

A(("q", "Q. Why a database per service?"))
A(("p",
   "\"So services stay independently deployable and cannot couple through shared tables. "
   "The cost is that there are no foreign keys across boundaries &mdash; a product holds a "
   "`manufacturerId` UUID with no database constraint tying it to the users table &mdash; "
   "so referential integrity across services becomes an application concern.\""))

A(("q", "Q. What is the N+1 problem and how do you avoid it?"))
A(("p", "Section 4.3. Mention `JOIN FETCH` or entity graphs, and that `open-in-view` is disabled so the problem surfaces in development rather than production."))

A(("q", "Q. Why is `ddl-auto: update` dangerous?"))
A(("p",
   "\"It never drops or narrows anything, so the schema silently drifts; it will not "
   "reliably alter a column's type or length; and there is no version history or "
   "rollback. I hit this directly &mdash; widening a column from 255 to 1024 characters "
   "required recreating the table. Flyway is the right answer.\""))

A(("q", "Q. Why UUIDs instead of auto-increment IDs?"))
A(("p", "Section 4.3: no information leakage, no enumeration, generated independently of the database. The cost is size and index locality."))

A(("h1", "8.6  Code quality and testing"))

A(("q", "Q. How did you test this?"))
A(("p",
   "\"96 unit tests &mdash; 73 across the four Java services and 23 on the Go chaincode at "
   "82.6% statement coverage &mdash; plus an end-to-end script that makes 41 HTTP assertions "
   "against the running stack with a live Fabric network. The tests I care most about are "
   "the ones on the fraud engine: that an unverifiable product is *reported* as counterfeit "
   "rather than throwing, and that a dependency outage is *not* reported as counterfeit. "
   "Those two are the core behaviour of the product.\""))

A(("q", "Q. How do you unit test something that calls two other services?"))
A(("p",
   "\"Mockito for the Feign clients, so I can simulate a 404 from the product service or a "
   "503 from the blockchain service without either running. That is only possible "
   "because of constructor injection &mdash; the class can be built in a test with mocks "
   "and no Spring context at all. The rule engine itself is a pure function over its "
   "inputs, so it needs no mocking whatsoever.\""))

A(("q", "Q. What is a DTO and why not return entities?"))
A(("p", "Section 4.2: over-exposure, mass assignment, lazy-loading explosions, and coupling the API to the schema."))

A(("h1", "8.7  The hard questions"))

A(("q", "Q. If a QR code can be photocopied, what actually stops a counterfeiter?"))
A(("p", "**Do not claim the QR code is secure. It is not, and it is not meant to be.**"))
A(("p",
   "\"Nothing stops them copying it &mdash; a QR code is a pointer, not a proof, and I "
   "designed it to carry only an opaque identifier for exactly that reason. What stops "
   "them profiting is that copying an identity does not copy the item. Once two physical "
   "objects share one identity, the scan history becomes physically impossible: the same "
   "product is scanned in two cities, or scanned as a first sale by five different "
   "people. That is what `DUPLICATE_QR` and `MULTIPLE_LOCATION_SCAN` detect. The "
   "blockchain is what makes that evidence trustworthy, because no single party can "
   "quietly edit it away.\""))

A(("q", "Q. Your fraud thresholds &mdash; 80, 35, 30 &mdash; where did those numbers come from?"))
A(("p", "**Do not pretend they are empirical.**"))
A(("p",
   "\"They are reasoned, not measured. The two structural rules score 100 because they are "
   "conclusive on their own. The behavioural rules are weighted so that any two together "
   "cross the suspicious threshold but not the counterfeit threshold, because no single "
   "behavioural signal should condemn a product outright. With real deployment data I "
   "would tune them against confirmed outcomes and measure the false-positive rate &mdash; "
   "which for this system matters more than false negatives, because wrongly rejecting "
   "genuine stock has an immediate commercial cost.\""))

A(("q", "Q. Where is the real bottleneck if this went to production?"))
A(("p",
   "\"Ledger writes. An endorse-order-commit round trip is on the order of a second or "
   "two, and it is synchronous in my registration flow. Reads are fine &mdash; verification "
   "queries the world state through a single peer. If throughput mattered I would batch "
   "registrations, or make registration asynchronous with the product starting `PENDING` "
   "and being promoted by a listener, which is precisely why that status field exists.\""))

A(("q", "Q. This is basically a CRUD app with a blockchain bolted on. Convince me otherwise."))
A(("p",
   "\"The catalogue is CRUD, and I would not pretend otherwise. What is not CRUD is that "
   "ownership rules are enforced by chaincode that every peer re-executes, so no single "
   "application server can write an invalid transfer; that verification is a "
   "multi-source decision combining the catalogue, the ledger and behavioural history "
   "into a scored verdict; and that the failure semantics distinguish 'cannot verify' "
   "from 'not genuine'. Take the blockchain away and the first two of those collapse into "
   "'trust my server', which is exactly what the domain cannot assume.\""))

A(("q", "Q. What is the weakest part of your system?"))
A(("p",
   "\"That one administrator controls both Fabric organisations. Two organisations do "
   "endorse every transaction, but since I operate both, the trust distribution is "
   "demonstrated rather than guaranteed. After that, the oracle problem &mdash; I cannot "
   "prove a physical object matches its digital identity. The first is a deployment "
   "change; the second is not solvable in software at all.\""))

A(("q", "Q. You did not write this alone, did you?"))
A(("p",
   "Answer with specifics rather than protest. Pick one decision and explain the "
   "reasoning chain behind it &mdash; why verification is read-only (5.5), or why a "
   "dependency outage must not be reported as a counterfeit verdict (3.6). Detailed "
   "reasoning about *trade-offs* is very difficult to fake, and it is a far more "
   "convincing answer than insisting."))

# ===========================================================================
# PART IX
# ===========================================================================

A(("part", "Part IX — Cheat Sheet and Glossary"))

A(("h1", "9.1  Draw this from memory"))
A(("p", "If you can reproduce these four diagrams on a whiteboard, you can hold any conversation about the project."))
A(("h2", "1. The architecture"))
A(("code", """
   React SPA
      |
      +---> auth :8081 -------> zerofake_auth        (issues the JWT)
      +---> product :8082 ----> zerofake_product     (catalogue, QR)
      +---> blockchain :8083 -> zerofake_blockchain -> Hyperledger Fabric
      +---> fraud :8085 ------> zerofake_fraud       (rules, scan history)

   blockchain --> product          (mark REGISTERED)
   fraud      --> product          (does this product exist?)
   fraud      --> blockchain       (is it on the ledger? history?)
"""))
A(("h2", "2. The verification decision"))
A(("code", """
   not in catalogue?  -> PRODUCT_NOT_FOUND     risk 100 -> COUNTERFEIT
   not on ledger?     -> BLOCKCHAIN_MISMATCH   risk 100 -> COUNTERFEIT
   otherwise, sum:    INVALID_OWNER            40
                      MULTIPLE_LOCATION_SCAN   35
                      DUPLICATE_QR             30
                      EXPIRED_PRODUCT          25
                      SUSPICIOUS_ACTIVITY      15
   cap at 100    >= 80 COUNTERFEIT   >= 20 SUSPICIOUS   else GENUINE
   dependency down -> 502, never a verdict
"""))
A(("h2", "3. The deployed topology"))
A(("code", """
   internet -> :443 nginx -> /            frontend
                          -> /auth/       auth-service:8081
                          -> /products/   product-service:8082
                          -> /blockchain/ blockchain-service:8083
                          -> /fraud/      fraud-service:8085

   one origin, so no CORS.  database and service ports unpublished.
   Fabric runs alongside on the same host.
"""))
A(("h2", "4. The Fabric transaction lifecycle"))
A(("code", """
   PROPOSE -> ENDORSE (simulate, read-write set) -> ORDER (block)
           -> VALIDATE (policy + read-set versions) -> COMMIT
"""))

A(("h1", "9.2  Numbers worth remembering"))
A(("table", [
    ["Fact", "Value"],
    ["Services / databases", "4 / 4"],
    ["Ports", "auth 8081, product 8082, blockchain 8083, fraud 8085"],
    ["Roles", "6 &mdash; admin, manufacturer, warehouse, distributor, retailer, customer"],
    ["Fraud rules", "7"],
    ["Risk thresholds", "80 counterfeit, 20 suspicious"],
    ["Access / refresh token life", "15 minutes / 7 days"],
    ["Unit tests", "96 &mdash; 73 Java, 23 Go chaincode"],
    ["Chaincode coverage", "82.6% of statements"],
    ["Smoke-test assertions", "41, all passing against a live ledger"],
    ["Chaincode transactions", "4 &mdash; register, transfer, verify, history"],
    ["Fabric / Gateway version", "2.5 LTS / 1.11"],
], [40, 60]))

A(("h1", "9.3  Glossary"))
A(("table", [
    ["Term", "Meaning"],
    ["**Chaincode**", "Fabric's term for a smart contract; the business logic every peer executes"],
    ["**World state**", "Key-value database holding the current value of each ledger key"],
    ["**Endorsement policy**", "Rule stating which organisations must approve a transaction for it to be valid"],
    ["**Read-write set**", "The keys a simulated transaction read (with versions) and would write"],
    ["**MVCC conflict**", "Validation failure because a key's version changed after it was read"],
    ["**MSP**", "Membership Service Provider &mdash; maps X.509 certificates to organisations and roles"],
    ["**Orderer**", "Node that sequences transactions into blocks; runs Raft"],
    ["**Channel**", "A private ledger shared by a defined subset of organisations"],
    ["**JWT**", "JSON Web Token: signed, Base64URL-encoded claims. Encoded, not encrypted"],
    ["**Claim**", "A key-value assertion inside a JWT payload, such as `role`"],
    ["**HS256**", "HMAC-SHA256 signing &mdash; one shared secret signs and verifies"],
    ["**BCrypt**", "Deliberately slow, automatically salted password hashing function"],
    ["**DTO**", "Data Transfer Object &mdash; the API's shape, kept separate from the entity"],
    ["**Dirty checking**", "Hibernate auto-detecting changes to managed entities and writing updates"],
    ["**N+1**", "One query for a collection, then one more per element &mdash; a lazy-loading trap"],
    ["**Oracle problem**", "A blockchain cannot verify that data entered into it was true in the real world"],
    ["**Soft delete**", "Marking a row inactive instead of removing it"],
    ["**Idempotent**", "Repeating the operation leaves the same state as doing it once"],
], [22, 78]))

A(("space", 14))
A(("key", "The last thing to remember",
   "You are not being tested on whether the system is perfect. You are being tested on "
   "whether you understand what you built, why you built it that way, and where it "
   "breaks. Every limitation in this document is one you can state confidently, with the "
   "reasoning behind it and what you would do next &mdash; and that is what a good engineer "
   "sounds like."))
