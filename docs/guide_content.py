"""
Content for the ZeroFake interview and concepts guide.

Every block is a tuple whose first element names its kind:
    ("part", text) ("h1", text) ("h2", text) ("p", text)
    ("ul", [items]) ("ol", [items]) ("code", text)
    ("table", [[header...], [row...]], [relative widths])
    ("warn"|"tip"|"key", title, text)
    ("q", question) ("toc1"|"toc2", text) ("pagebreak",) ("space", pts)
"""

DOC_TITLE = "ZeroFake"
DOC_SUBTITLE = "Architecture, Workflow & Interview Preparation Guide"

CONTENT = []
A = CONTENT.append

# ===========================================================================
# How to use / contents
# ===========================================================================

A(("pagebreak",))
A(("h1", "How to use this document"))
A(("p",
   "This guide has one job: make sure that nothing about ZeroFake can be asked "
   "that you have not already thought about. It is written to be read in order "
   "once, then used as a reference."))
A(("p",
   "Every technical claim here matches the code as it actually stands. Where the "
   "implementation has a limitation, this document says so plainly rather than "
   "glossing over it. That is deliberate: in a viva, the fastest way to lose "
   "credibility is to over-claim and get caught, and the fastest way to gain it "
   "is to name a weakness before the examiner does."))
A(("ul", [
    "**Part I** is the pitch and the problem. Read it until you can say it without notes.",
    "**Parts II and III** are the architecture and the end-to-end workflows.",
    "**Part IV** is the concept encyclopaedia: every technology used, explained from first principles.",
    "**Part V** is the design decisions and their trade-offs. This is where marks are won.",
    "**Parts VI and VII** are the security analysis and the honest limitations.",
    "**Part VIII** is a large bank of interview questions with model answers, ending with the hard ones.",
    "**Part IX** is a one-page whiteboard cheat sheet and a glossary.",
]))
A(("tip", "The single most valuable habit",
   "For every feature, be ready to answer three questions in sequence: *what does it do*, "
   "*why did you build it that way*, and *what would you do differently now*. Most students "
   "can answer the first. Answering all three is what makes an examiner conclude that you "
   "designed the system rather than assembled it."))

A(("h1", "Contents"))
A(("toc1", "Part I &mdash; The Project"))
A(("toc2", "The problem  &middot;  What ZeroFake does  &middot;  The pitch  &middot;  Technology stack"))
A(("toc1", "Part II &mdash; Architecture"))
A(("toc2", "System diagram  &middot;  Why microservices  &middot;  Service boundaries  &middot;  Data architecture  &middot;  Communication  &middot;  API reference"))
A(("toc1", "Part III &mdash; The Workflows"))
A(("toc2", "Registration and login  &middot;  Product creation  &middot;  Ledger registration  &middot;  Ownership transfer  &middot;  Customer verification  &middot;  Failure paths"))
A(("toc1", "Part IV &mdash; Core Concepts"))
A(("toc2", "Authentication and authorisation  &middot;  Spring Boot  &middot;  JPA and PostgreSQL  &middot;  Blockchain fundamentals  &middot;  Hyperledger Fabric  &middot;  Fraud detection  &middot;  QR codes  &middot;  REST  &middot;  Docker"))
A(("toc1", "Part V &mdash; Design Decisions and Trade-offs"))
A(("toc1", "Part VI &mdash; Security Analysis"))
A(("toc1", "Part VII &mdash; Limitations and Future Work"))
A(("toc1", "Part VIII &mdash; Interview Question Bank"))
A(("toc2", "Warm-up  &middot;  Architecture  &middot;  Security  &middot;  Blockchain  &middot;  Data  &middot;  Testing  &middot;  The hard questions"))
A(("toc1", "Part IX &mdash; Whiteboard Cheat Sheet and Glossary"))

# ===========================================================================
# PART I
# ===========================================================================

A(("part", "Part I \u2014 The Project"))

A(("h1", "1.1  The problem"))
A(("p",
   "Counterfeiting is not a niche crime. The OECD has repeatedly estimated that "
   "counterfeit and pirated goods account for a low single-digit percentage of world "
   "trade, and the categories where it hurts most are the ones where a fake is "
   "dangerous rather than merely disappointing: medicines, automotive parts, "
   "electrical components, cosmetics."))
A(("p",
   "The structural reason counterfeiting works is that **a physical product carries no "
   "trustworthy proof of its own origin**. A label can be printed. A hologram can be "
   "cloned. A serial number can be copied onto a thousand fakes. Whatever the "
   "manufacturer stamps on the box, the counterfeiter can stamp too."))
A(("p",
   "So the interesting question is not \"how do we make a label that cannot be copied\" "
   "&mdash; that is a materials-science arms race &mdash; but **\"how do we make copying a "
   "label useless?\"** That reframing is the whole idea behind ZeroFake."))

A(("key", "The core insight",
   "If the authoritative record of a product lives in a shared, append-only ledger that "
   "no single party can rewrite, then copying the label does not copy the product's "
   "identity. Two items bearing the same code become *detectably* inconsistent, because "
   "the ledger and the scan history can only tell one coherent story."))

A(("h1", "1.2  What ZeroFake does"))
A(("p",
   "ZeroFake gives every manufactured item a unique identity that is anchored on a "
   "Hyperledger Fabric ledger at the moment of manufacture, and then records every "
   "change of custody as it moves down the supply chain:"))
A(("code", """
   Manufacturer  ->  Warehouse  ->  Distributor  ->  Retailer  ->  Customer
        |              |               |              |             |
        +--------------+---------------+--------------+-------------+
                                  |
                    every transfer is a ledger transaction
"""))
A(("p",
   "A customer scans the QR code on the item. The platform then answers a single "
   "question &mdash; *is this genuine?* &mdash; by checking three independent things:"))
A(("ol", [
    "**Does this product exist at all?** If the catalogue has never heard of the identifier, the item is counterfeit. Nothing else needs checking.",
    "**Does it have a blockchain identity?** A product can exist on paper but have no ledger record, which is what happens when a counterfeiter copies a real product code onto a fake item.",
    "**Does its scan history make physical sense?** One physical object cannot be in Bengaluru and Mumbai at the same time, and cannot be legitimately scanned as 'first sale' by five different people.",
]))
A(("p",
   "The third check is the one that catches the hardest case: a counterfeiter who "
   "photographs a genuine QR code and reprints it on fakes. The ledger says the code is "
   "real, because it is. What gives the fakes away is that the *same* identity starts "
   "appearing in impossible places."))

A(("h1", "1.3  The pitch"))
A(("h2", "Thirty seconds"))
A(("p",
   "\"ZeroFake is an anti-counterfeiting platform. Every product gets a unique identity "
   "written to a Hyperledger Fabric blockchain when it is manufactured, and every "
   "change of ownership down the supply chain is recorded as a ledger transaction. A "
   "customer scans a QR code and the system verifies the product against that ledger, "
   "then runs a rule-based fraud engine over its scan history to catch cloned codes. "
   "It is four Spring Boot microservices, a React front end, PostgreSQL, and Go "
   "chaincode, all deployable with Docker Compose.\""))

A(("h2", "Two minutes"))
A(("p",
   "Open with the problem framing from section 1.1 &mdash; that a label cannot prove its "
   "own authenticity, so the goal is to make copying it useless. Then walk the three "
   "checks. Then give the architecture in one breath: an authentication service that "
   "issues JWTs, a product service that owns the catalogue and generates QR codes, a "
   "blockchain service that is the only component allowed to talk to Fabric, and a "
   "fraud detection service that orchestrates verification. Finish on a trade-off you "
   "chose deliberately &mdash; the oracle problem in section 5.9 is the strongest one to "
   "raise yourself."))

A(("tip", "Always volunteer a limitation",
   "Ending the pitch with \"the honest limitation is that the blockchain proves the "
   "*identity* is genuine, not that the *physical object* matches it &mdash; that is the "
   "oracle problem, and no blockchain solves it\" is worth more than any additional "
   "feature you could describe. It tells the examiner you understand the boundary of "
   "your own solution."))

A(("h1", "1.4  Technology stack and why each piece is there"))
A(("table", [
    ["Layer", "Technology", "Why this, specifically"],
    ["Backend", "Java 21, Spring Boot 3.5",
     "Mature ecosystem for REST and security; Fabric has a first-class Java gateway client. Java 21 for records, pattern matching and virtual-thread readiness."],
    ["Security", "Spring Security 6, JJWT 0.12",
     "Filter-chain model maps cleanly onto stateless token auth; JJWT is the standard JWT library for Java."],
    ["Persistence", "Spring Data JPA, Hibernate, PostgreSQL 16",
     "Relational integrity matters for a catalogue with real foreign keys; PostgreSQL is free, strict, and ubiquitous."],
    ["Mapping", "MapStruct 1.6",
     "Compile-time generated mappers &mdash; no reflection cost, and mapping errors surface at build time rather than at runtime."],
    ["Inter-service", "Spring Cloud OpenFeign",
     "Declarative HTTP clients: the contract is a Java interface, which keeps the call sites readable."],
    ["Blockchain", "Hyperledger Fabric 2.5 LTS",
     "Permissioned. Known participants, no cryptocurrency, no proof-of-work, high throughput. See section 4.4 for why not Ethereum."],
    ["Chaincode", "Go, Fabric Contract API v2",
     "Go is the best-supported chaincode language on Fabric and compiles to a single static binary."],
    ["Ledger client", "Fabric Gateway 1.11",
     "The modern client API. The older `fabric-sdk-java` is deprecated; using it would be a mark against the project."],
    ["QR", "ZXing 3.5",
     "The reference open-source QR implementation on the JVM."],
    ["Frontend", "React 19, Vite, TypeScript, Tailwind 4",
     "TypeScript matters here: it caught every backend contract change at compile time during development."],
    ["Deployment", "Docker, Docker Compose",
     "One command reproduces the whole stack. Kubernetes was deliberately not used &mdash; see section 5.10."],
], [12, 22, 66]))

# ===========================================================================
# PART II
# ===========================================================================

A(("part", "Part II \u2014 Architecture"))

A(("h1", "2.1  The system at a glance"))
A(("code", """
                          +---------------------------+
                          |   React SPA (browser)     |
                          |   QR scanner, dashboards  |
                          +-------------+-------------+
                                        |  HTTPS + Bearer token
        +-------------------+-----------+-----------+-------------------+
        |                   |                       |                   |
   +----v-----+      +------v------+        +-------v------+    +-------v--------+
   |   auth   |      |   product   |        |  blockchain  |    |     fraud      |
   |  :8081   |      |    :8082    |        |    :8083     |    |     :8085      |
   +----+-----+      +------+------+        +-------+------+    +-------+--------+
        |                   |    ^                  |    ^  ^           |
        |                   |    |  mark REGISTERED |    |  |           |
        |                   |    +------------------+    |  +-----------+
        |                   |                            |     verify / history
        |                   |         product lookup     |
        |                   +<---------------------------|---------------+
        |                                                |
   +----v-----+      +------v------+        +-------v------+    +-------v--------+
   | zerofake |      |  zerofake   |        |   zerofake   |    |    zerofake    |
   |  _auth   |      |  _product   |        |  _blockchain |    |     _fraud     |
   +----------+      +-------------+        +------+-------+    +----------------+
        PostgreSQL: one database per service        |
                                            +-------v---------+
                                            | Hyperledger     |
                                            | Fabric 2.5      |
                                            | peer + orderer  |
                                            +-----------------+
"""))
A(("p",
   "Four services, four databases, one ledger. The arrows that matter are the ones "
   "*between* services: blockchain calls product to promote a product's status, and "
   "fraud calls both product and blockchain during verification. Nothing else crosses."))

A(("h1", "2.2  Why microservices &mdash; and the honest counter-argument"))
A(("p",
   "The reasons that genuinely apply to this project:"))
A(("ul", [
    "**Failure isolation.** The Fabric gateway is the least reliable component in the system: it depends on an external network of peers and orderers. Isolating it means a peer outage degrades verification rather than taking the catalogue offline with it.",
    "**Independent scaling.** Verification is a customer-facing, read-heavy, potentially high-volume path. Product administration is used by a handful of manufacturer staff. These have no reason to scale together.",
    "**Boundary enforcement.** A process boundary is the only boundary juniors cannot accidentally cross. In a monolith, nothing stops someone importing the Fabric gateway into a controller; across services it is impossible without a deliberate HTTP call.",
    "**Independent deployability.** Changing a fraud rule redeploys one service, not the entire platform.",
]))
A(("warn", "Be ready for the counter-argument",
   "A good examiner will say: *\"this is a college project with four services and one "
   "team &mdash; microservices are overkill, you have added network calls, distributed "
   "failure modes and eventual consistency for no operational benefit.\"* **They are "
   "substantially right, and the correct response is to agree and then defend the "
   "specific boundary that earns its keep.** Say: \"For a system at this scale a modular "
   "monolith would be simpler and I would lose almost nothing. The one split I would "
   "keep regardless is the blockchain service, because it isolates an unreliable "
   "external dependency and it is the only component holding the Fabric signing "
   "identity. The other splits are as much about demonstrating the pattern as about "
   "operational need.\" Conceding the point and keeping one well-argued boundary is a "
   "far stronger answer than defending all four."))

A(("h1", "2.3  Service boundaries &mdash; who owns what"))
A(("p",
   "The rule that keeps this architecture coherent is that **each service owns exactly one "
   "kind of truth**, and no service reaches into another's data."))
A(("table", [
    ["Service", "Owns", "Must never"],
    ["**auth** :8081",
     "Users, roles, password hashes, refresh tokens. Issues and signs every JWT.",
     "Know that products or blockchains exist."],
    ["**product** :8082",
     "Catalogue: products, categories, manufacturing batches, QR code images, and the local projection of blockchain status.",
     "Talk to Fabric. Manage users. Decide whether something is fraudulent."],
    ["**blockchain** :8083",
     "The Fabric gateway connection, the signing identity, all chaincode invocation, and the local audit trail of ledger transactions.",
     "Own product CRUD or users. Contain any fraud logic."],
    ["**fraud** :8085",
     "Verification workflow, the seven fraud rules, risk scoring, scan history, verification logs, fraud reports.",
     "**Talk to Fabric directly** &mdash; it must go through the blockchain service. Write to the ledger at all."],
], [16, 46, 38]))
A(("key", "The boundary that carries the most weight",
   "Fraud detection never touches Hyperledger Fabric directly. It asks the blockchain "
   "service, over HTTP. This means the Fabric identity, the gRPC connection and the "
   "chaincode contract exist in exactly one place. If that rule were relaxed, two "
   "services would hold signing credentials and the blast radius of a compromise would "
   "double."))

A(("h1", "2.4  Data architecture: database per service"))
A(("p",
   "Each service has its own PostgreSQL database and no service issues a query against "
   "another's tables. In local deployment all four databases live inside a single "
   "PostgreSQL container, created at start-up by an init script &mdash; separate databases, "
   "shared server, which is the normal compromise for development."))
A(("p", "The consequence you must be ready to discuss is that **there are no foreign keys "
        "across services**. A product row holds a `manufacturerId` UUID, but there is no "
        "database-level constraint tying it to the users table, because that table is in "
        "another database owned by another service. Referential integrity across a service "
        "boundary becomes an application concern."))
A(("table", [
    ["Database", "Principal tables"],
    ["`zerofake_auth`", "`users`, `refresh_tokens`"],
    ["`zerofake_product`", "`products`, `product_categories`, `product_batches`"],
    ["`zerofake_blockchain`", "`blockchain_transactions`"],
    ["`zerofake_fraud`", "`scan_history`, `verification_logs`, `fraud_reports`"],
], [30, 70]))

A(("h1", "2.5  How services talk to each other"))
A(("p",
   "All inter-service communication is **synchronous HTTP over the internal Docker "
   "network**. There is no message broker: verification needs an answer before it can "
   "respond to the user, so an asynchronous queue would add latency and complexity "
   "while solving nothing."))
A(("h2", "Token propagation &mdash; the important detail"))
A(("p",
   "When the fraud service calls the product service, it **forwards the caller's own "
   "bearer token** rather than using a service account. A Feign request interceptor "
   "copies the incoming `Authorization` header onto the outgoing request; the blockchain "
   "service does the same thing when it calls the product service."))
A(("p", "Two things follow, and both are worth stating in an interview:"))
A(("ul", [
    "**No service holds an ambient privilege.** There is no super-user credential that a compromised service could abuse. Every downstream call carries the identity of the human who initiated it.",
    "**Authorisation is evaluated consistently.** The product service applies exactly the same role rules whether the request came from the browser or from another service, because it cannot tell the difference and does not need to.",
]))
A(("code", """
Browser  --[Bearer alice]-->  fraud service
                                   |
                                   +--[Bearer alice]-->  product service
                                   |                     (applies alice's roles)
                                   +--[Bearer alice]-->  blockchain service
                                                         (applies alice's roles)
"""))
A(("warn", "The trade-off in this choice",
   "Token propagation means a downstream service can do anything the *user* can do, and "
   "the user's token is now present in more processes. The alternative &mdash; per-service "
   "credentials with their own scopes &mdash; is more precise but requires a service "
   "identity system. For this scale, propagation is the right call; be ready to say why "
   "you would revisit it if services started acting on their own behalf, such as in a "
   "scheduled batch job where there is no user to propagate."))

A(("h1", "2.6  API reference"))
A(("table", [
    ["Method &amp; path", "Service", "Who may call it"],
    ["`POST /api/v1/auth/register`", "auth", "Public &mdash; but only `ROLE_CUSTOMER`; privileged roles need an admin token"],
    ["`POST /api/v1/auth/login`", "auth", "Public"],
    ["`POST /api/v1/auth/refresh`", "auth", "Public (the refresh token is the credential)"],
    ["`GET /api/v1/auth/me`", "auth", "Any authenticated user"],
    ["`POST /api/v1/auth/logout`", "auth", "Any authenticated user"],
    ["`GET /api/v1/products`", "product", "Any authenticated user"],
    ["`POST /api/v1/products`", "product", "ADMIN, MANUFACTURER"],
    ["`GET /api/v1/products/{id}/qr-code`", "product", "Any authenticated user (returns PNG)"],
    ["`PATCH /api/v1/products/{id}/blockchain-status`", "product", "ADMIN, MANUFACTURER &mdash; called by the blockchain service"],
    ["`POST /api/v1/categories`", "product", "ADMIN only"],
    ["`POST /api/v1/batches`", "product", "ADMIN, MANUFACTURER"],
    ["`POST /api/v1/blockchain/register-product`", "blockchain", "ADMIN, MANUFACTURER"],
    ["`POST /api/v1/blockchain/transfer-ownership`", "blockchain", "ADMIN and all four supply-chain roles"],
    ["`POST /api/v1/blockchain/verify-product`", "blockchain", "Any authenticated user (read-only)"],
    ["`GET /api/v1/blockchain/products/{id}/history`", "blockchain", "Any authenticated user"],
    ["`POST /api/v1/fraud/verify`", "fraud", "Any authenticated user"],
    ["`POST /api/v1/fraud/reports`", "fraud", "Any authenticated user"],
    ["`GET /api/v1/fraud/reports`, `/logs`, `/scans`", "fraud", "ADMIN, MANUFACTURER only"],
], [46, 16, 38]))
A(("p", "Every endpoint returns the same envelope, which keeps client-side error handling uniform:"))
A(("code", """
{
  "timestamp": "2026-08-10T17:48:53.012",
  "status":    200,
  "success":   true,
  "message":   "Product verification completed.",
  "data":      { ... the actual payload, or null ... }
}
"""))

# ===========================================================================
# PART III
# ===========================================================================

A(("part", "Part III \u2014 The Workflows"))

A(("p",
   "This part traces what actually happens, end to end. If you can narrate these five "
   "flows from memory you can answer almost any \"walk me through what happens when...\" "
   "question."))

A(("h1", "3.1  Registration and login"))
A(("ol", [
    "The browser posts email, password and requested role to `POST /api/v1/auth/register`.",
    "The service rejects the request with **400** if a non-customer role is requested without an admin token. Self-service registration can only create customers &mdash; a manufacturer account is a position of trust in the supply chain and must be granted, not claimed.",
    "It rejects with **409** if the email already exists.",
    "The password is hashed with **BCrypt** and the user is saved with status `ACTIVE`.",
    "At `POST /api/v1/auth/login`, Spring Security's `AuthenticationManager` loads the user and compares the presented password against the stored hash.",
    "On success the service issues **two** tokens: a 15-minute access token and a 7-day refresh token. The refresh token is also persisted, so it can be revoked.",
    "`lastLogin` is stamped and both tokens plus the user profile are returned.",
]))
A(("p", "The access token payload looks like this &mdash; note the two custom claims, which are what make the rest of the architecture work:"))
A(("code", """
{
  "userId": "3f2a91c4-...-8be1",     <- custom claim: who, as a UUID
  "role":   "ROLE_MANUFACTURER",     <- custom claim: what they may do
  "sub":    "manufacturer@zerofake.com",
  "iss":    "zerofake-auth-service",
  "iat":    1786384300,
  "exp":    1786385200
}
"""))
A(("key", "Why those two claims exist",
   "Without `userId` and `role` in the token, every downstream service would have to call "
   "the authentication service on every single request to find out who the caller is. "
   "That would make auth a synchronous dependency of everything and a single point of "
   "failure. Putting the claims in the signed token is what makes the other three "
   "services genuinely independent."))

A(("h1", "3.2  Creating a product and its QR code"))
A(("ol", [
    "A manufacturer posts to `POST /api/v1/products`. The security filter chain has already rejected the request if the token does not carry `ROLE_MANUFACTURER` or `ROLE_ADMIN`.",
    "The service checks the product code is unique (**409** if not) and that the category exists (**404** if not).",
    "The product is saved. Hibernate generates its UUID primary key.",
    "**Only now** can the QR code be generated, because the QR encodes the generated identifier. A second save stores the resulting path.",
    "The product starts with `blockchainStatus = PENDING`. It exists in the catalogue but has no ledger identity yet.",
]))
A(("p",
   "The QR code encodes **the product UUID and nothing else**. That is a deliberate "
   "decision covered in section 4.7: encoding richer data would invite the reader to "
   "trust the label instead of the ledger."))

A(("h1", "3.3  Registering the product on the ledger"))
A(("ol", [
    "The manufacturer posts to `POST /api/v1/blockchain/register-product`.",
    "The service first checks its own audit trail for an existing successful registration. If one exists it returns **409** rather than writing a duplicate.",
    "It builds a chaincode proposal for `RegisterProduct` and **reads the transaction ID from the proposal before sending it**. This matters: the identifier is known even if the submission then fails.",
    "`proposal.endorse()` sends the proposal to the peers, which simulate it and return signed endorsements.",
    "`transaction.submitAsync()` sends the endorsed transaction to the ordering service.",
    "`submitted.getStatus()` blocks until the transaction is committed to a block, and returns the **block number** and a validation code.",
    "A local audit record is written with the real transaction ID, the real block number and status `SUCCESS`.",
    "The blockchain service then calls the product service to promote the product to `REGISTERED`, forwarding the manufacturer's token.",
]))
A(("warn", "The step that is easy to get wrong",
   "That final status update is *best-effort* and deliberately does not fail the request. "
   "The ledger write has already committed and cannot be undone; the catalogue status is "
   "a local projection that can be repaired. Failing the whole request because a "
   "secondary update failed would tell the manufacturer their registration failed when "
   "in fact it is permanently on the blockchain &mdash; the worst possible lie to tell. "
   "This is an example of choosing which side of an inconsistency to fall on."))

A(("h1", "3.4  Transferring ownership"))
A(("p",
   "Each handover is an explicit action by the party giving up custody, calling "
   "`POST /api/v1/blockchain/transfer-ownership`. The chaincode enforces the rules that "
   "actually matter, because that is the only place they cannot be bypassed:"))
A(("ul", [
    "The product must exist on the ledger.",
    "The claimed current owner must **actually be** the current owner on the ledger, otherwise the transaction is rejected.",
    "The new owner must differ from the current owner.",
    "The new role must be one of the five valid supply chain roles.",
    "Product status is derived from the new role: a transfer to a customer sets `DELIVERED`, to an intermediary sets `IN_TRANSIT`.",
]))
A(("key", "Why validation belongs in the chaincode",
   "Validation in the Java service is a *convenience*; validation in the chaincode is a "
   "*guarantee*. Every peer independently re-executes the chaincode and must agree, so a "
   "compromised or buggy application server cannot write an invalid ownership transfer. "
   "This is the single clearest example of what the blockchain buys you, and it is an "
   "excellent thing to say out loud."))

A(("h1", "3.5  Customer verification &mdash; the core flow"))
A(("p", "This is the flow to know cold. Everything else in the project exists to support it."))
A(("code", """
  customer scans QR  ->  POST /api/v1/fraud/verify  { productId, location, device }
                              |
                              |  scanner identity comes from the JWT, NOT the body
                              v
        +--------------------------------------------------+
        | 1. Does the product exist in the catalogue?       |
        |    404 from product service -> PRODUCT_NOT_FOUND  | -> risk 100, STOP
        +--------------------------------------------------+
                              v
        +--------------------------------------------------+
        | 2. Does it exist on the ledger?                   |
        |    404 from blockchain svc -> BLOCKCHAIN_MISMATCH | -> risk 100, STOP
        +--------------------------------------------------+
                              v
        +--------------------------------------------------+
        | 3. Evaluate the remaining five rules against      |
        |    on-chain ownership, scan history, batch dates  |
        +--------------------------------------------------+
                              v
        +--------------------------------------------------+
        | 4. Sum the risk. Cap at 100. Classify.            |
        |      >= 80  COUNTERFEIT     >= 20  SUSPICIOUS     |
        |                             else   GENUINE        |
        +--------------------------------------------------+
                              v
        +--------------------------------------------------+
        | 5. Persist: scan history + verification log,      |
        |    and a fraud report if risk >= 80               |
        +--------------------------------------------------+
"""))
A(("p", "Three properties of this flow are worth memorising, because each one is a likely question:"))
A(("ul", [
    "**The scanner's identity comes from the token, never the request body.** If the client could supply its own user ID, anyone could attribute scans to someone else, and every rule built on scan history would be forgeable.",
    "**The whole flow is read-only with respect to the supply chain.** Verification writes only to fraud's own audit tables. Scanning an item never transfers its ownership. Inspection and custody are different acts &mdash; see section 5.5.",
    "**A dependency outage is never reported as a counterfeit verdict.** If the product or blockchain service is unreachable, the response is **502** with \"authenticity cannot be confirmed\", not a verdict. This distinction is discussed in section 3.6 and is the single most important error-handling decision in the project.",
]))

A(("h1", "3.6  Failure paths &mdash; and the distinction that matters most"))
A(("p",
   "In an anti-counterfeiting system there are two completely different negative "
   "outcomes, and conflating them is the worst bug the system can have:"))
A(("table", [
    ["Situation", "HTTP", "What the user is told"],
    ["Product not in catalogue", "200", "**COUNTERFEIT** &mdash; a verdict, risk 100"],
    ["Product not on the ledger", "200", "**COUNTERFEIT** &mdash; a verdict, risk 100"],
    ["Product service unreachable", "502", "\"The product catalogue is unavailable, so authenticity cannot be confirmed.\""],
    ["Blockchain service unreachable", "502", "\"The blockchain service is unavailable, so authenticity cannot be confirmed.\""],
    ["Fabric peer down", "502", "\"Unable to connect to the Hyperledger Fabric network.\""],
], [34, 10, 56]))
A(("key", "\"We could not check\" is not \"we checked and it is fake\"",
   "A false counterfeit verdict caused by an outage would destroy trust in the platform "
   "and could see genuine stock rejected. A verdict is only ever returned when the "
   "system successfully established a fact. Everything else is an error with a distinct "
   "status code. In an earlier version of this codebase a missing ledger record produced "
   "an HTTP 500 &mdash; which meant the `BLOCKCHAIN_MISMATCH` rule could never fire at all, "
   "and the system crashed at exactly the moment it should have detected a fake."))

# ===========================================================================
# PART IV
# ===========================================================================

A(("part", "Part IV \u2014 Core Concepts"))

A(("p",
   "This part explains every technology in the project from first principles. If a "
   "question starts \"what is...\" or \"how does... work\", the answer is here."))

# --- 4.1 auth --------------------------------------------------------------

A(("h1", "4.1  Authentication and authorisation"))

A(("h2", "The distinction"))
A(("ul", [
    "**Authentication** answers *who are you?* &mdash; proving identity, usually with a password or a token.",
    "**Authorisation** answers *what are you allowed to do?* &mdash; evaluated after identity is established.",
]))
A(("p",
   "In ZeroFake, authentication happens once at login and is then carried by a token; "
   "authorisation happens on every request, in every service, from the token's `role` claim."))

A(("h2", "Sessions versus tokens"))
A(("p",
   "Traditional session authentication stores server-side state: the browser holds an "
   "opaque session ID and the server looks it up. That requires either sticky sessions "
   "or a shared session store &mdash; and with four independent services it would mean all "
   "four sharing session state, which couples them tightly."))
A(("p",
   "**Token-based authentication is stateless.** The token itself carries the identity "
   "claims and is cryptographically signed. Any service holding the signing key can "
   "verify it without a lookup and without shared state. That property is exactly what "
   "makes independent services practical."))

A(("h2", "JWT anatomy"))
A(("p", "A JWT is three Base64URL-encoded segments joined by dots:"))
A(("code", """
  eyJhbGciOiJIUzI1NiJ9  .  eyJ1c2VySWQiOiIzZjJhLi4uIn0  .  4Xk9_mQ2vB...
  \\_______________/        \\____________________/        \\__________/
       HEADER                     PAYLOAD                  SIGNATURE
   algorithm, type          the claims (identity)      proof of integrity
"""))
A(("ul", [
    "**Header** &mdash; names the signing algorithm, here `HS256`.",
    "**Payload** &mdash; the claims. Registered claims like `sub` (subject), `iss` (issuer), `iat` (issued at) and `exp` (expiry) have standard meanings; `userId` and `role` are our custom claims.",
    "**Signature** &mdash; `HMAC-SHA256(base64(header) + \".\" + base64(payload), secret)`.",
]))
A(("warn", "A JWT is encoded, not encrypted",
   "Base64 is not encryption. Anyone holding the token can read every claim in it &mdash; "
   "paste one into jwt.io and it decodes instantly. The signature prevents *tampering*, "
   "not *reading*. This is why nothing secret ever goes in a payload, and why the project "
   "has a unit test asserting that the password hash never appears in a token. Expect to "
   "be asked \"is a JWT encrypted?\" &mdash; the answer is no, and knowing that is a "
   "differentiator."))

A(("h2", "HS256 versus RS256"))
A(("table", [
    ["", "HS256 (used here)", "RS256"],
    ["Key type", "One shared secret", "Private key signs, public key verifies"],
    ["Who can sign", "Anyone holding the secret &mdash; all four services", "Only the holder of the private key"],
    ["Who can verify", "Same shared secret", "Anyone, using the public key"],
    ["Right when", "Few services, one trust domain, simple ops", "Many consumers, or verifiers you do not fully trust"],
], [16, 42, 42]))
A(("p",
   "**The honest trade-off:** because all four services share one secret, any of them "
   "could *forge* a token, not merely verify one. With RS256 only the authentication "
   "service could sign, and the others would hold a public key that is useless for "
   "forgery. For a single-team project inside one trust boundary HS256 is a reasonable "
   "simplification; if the services were operated by different teams, RS256 would be the "
   "correct choice. Volunteering this shows you understand the model rather than having "
   "copied it."))

A(("h2", "Access tokens and refresh tokens"))
A(("p",
   "The access token lives 15 minutes; the refresh token lives 7 days and is stored in "
   "the database so it can be revoked. The reason for the split is a genuine tension:"))
A(("ul", [
    "A **stateless** token cannot be revoked &mdash; nothing is checked at verification time, so a stolen token is valid until it expires. Short expiry bounds that damage.",
    "But making the user log in every 15 minutes is unusable. The refresh token bridges the gap: it is long-lived but **stateful** &mdash; it is looked up in the database, so logging out deletes it and it stops working immediately.",
]))
A(("p", "So the system gets fast stateless checks on the hot path and real revocation on the slow path."))

A(("h2", "Password storage: BCrypt"))
A(("p", "Passwords are hashed with BCrypt, never encrypted and never stored in the clear. Three properties matter:"))
A(("ul", [
    "**One-way.** Hashing cannot be reversed. A database leak does not directly yield passwords.",
    "**Salted.** BCrypt generates a random salt per password and stores it inside the hash string. Two users with the same password get different hashes, which defeats rainbow tables.",
    "**Deliberately slow.** A configurable work factor makes each hash cost ~100ms. This is a *feature*: it makes brute-forcing infeasible. This is precisely why SHA-256 is the wrong tool &mdash; it is fast by design, and speed is the attacker's friend here.",
]))
A(("code", """
  $2a$10$N9qo8uLOickgx2ZMRZoMy.MH/rniGVJPjZuQKVvQxrCsMlBjqiPfW
  \\__/\\_/ \\________________________/\\____________________________/
  algo cost         salt (22 chars)          hash (31 chars)
"""))

A(("h2", "The Spring Security filter chain"))
A(("p",
   "Spring Security is a chain of servlet filters running *before* your controllers. A "
   "request passes through each filter in order; any filter can reject it. ZeroFake "
   "inserts a custom `JwtAuthenticationFilter` before the username/password filter:"))
A(("code", """
  request
     |
     v
  CorsFilter ....................... is this origin allowed?
     |
     v
  JwtAuthenticationFilter .......... read Bearer token, validate signature,
     |                               build an Authentication and put it in
     |                               the SecurityContext
     v
  AuthorizationFilter .............. does the Authentication carry a role
     |                               permitted for this path?
     v
  DispatcherServlet -> Controller
"""))
A(("p",
   "If the token is missing or invalid, the filter simply leaves the context empty and "
   "lets the request continue; the authorisation filter then rejects it. The "
   "`AuthenticationEntryPoint` turns that rejection into a **401** JSON response, and the "
   "`AccessDeniedHandler` turns an insufficient-role rejection into a **403**."))
A(("tip", "401 versus 403",
   "**401 Unauthorized** actually means *unauthenticated* &mdash; we do not know who you "
   "are. **403 Forbidden** means we know exactly who you are and you still may not do "
   "this. The names are a historical mistake in the HTTP spec. Getting this right in "
   "conversation is a small, reliable signal of competence."))

A(("h2", "Role-based access control"))
A(("p", "Six roles, mapped to real positions in the supply chain:"))
A(("code", """
  ROLE_ADMIN          platform administrator, may do anything
  ROLE_MANUFACTURER   creates products, registers them on the ledger
  ROLE_WAREHOUSE      receives and forwards custody
  ROLE_DISTRIBUTOR    receives and forwards custody
  ROLE_RETAILER       receives custody, sells to customers
  ROLE_CUSTOMER       verifies products; the only self-registerable role
"""))
A(("p",
   "The `ROLE_` prefix is a Spring Security convention: `hasRole(\"ADMIN\")` looks for an "
   "authority literally named `ROLE_ADMIN`. This project uses `hasAuthority(\"ROLE_ADMIN\")` "
   "instead, which is the explicit form and avoids the invisible prefix rule."))

# --- 4.2 spring ------------------------------------------------------------

A(("h1", "4.2  Spring Boot and the Spring ecosystem"))

A(("h2", "Inversion of control and dependency injection"))
A(("p",
   "Rather than a class constructing its own collaborators, it declares what it needs "
   "and the framework supplies them. The container owns object creation and lifetime &mdash; "
   "control is inverted."))
A(("p", "ZeroFake uses **constructor injection** everywhere, via Lombok's `@RequiredArgsConstructor`. Compared with field injection this gives you:"))
A(("ul", [
    "**Immutability** &mdash; dependencies can be `final`.",
    "**Honesty** &mdash; a constructor with nine parameters is visibly a class doing too much. Field injection hides that.",
    "**Testability** &mdash; you can construct the class in a unit test with mocks and no Spring context at all, which is exactly how `VerificationServiceImplTest` works.",
]))

A(("h2", "Auto-configuration"))
A(("p",
   "`@SpringBootApplication` triggers a scan of the classpath. Finding PostgreSQL's "
   "driver, Spring Boot configures a `DataSource`; finding Spring Web, it starts an "
   "embedded Tomcat. Conditional annotations such as `@ConditionalOnMissingBean` mean "
   "any bean you define yourself wins over the default."))

A(("h2", "@Transactional and how it actually works"))
A(("p",
   "Spring implements `@Transactional` with a **proxy**. The injected object is not your "
   "class but a generated subclass that opens a transaction, delegates to your method, "
   "then commits or rolls back."))
A(("warn", "The self-invocation trap",
   "Because the proxy sits *outside* your object, calling one method of a class from "
   "another method of the same class bypasses it entirely &mdash; `this.doWork()` goes "
   "straight to the real method and the annotation does nothing. This is one of the most "
   "commonly asked Spring questions. Also note that Spring rolls back on unchecked "
   "exceptions by default, but **not** on checked ones unless you say `rollbackFor`."))
A(("p",
   "There is a deliberate example of *not* using a transaction in this project. When a "
   "ledger transaction fails, the blockchain service writes a `FAILED` audit record and "
   "then throws. If that write were inside the same transaction as the request, the "
   "throw would roll the record back and the failure would leave no trace. So each audit "
   "record is committed on its own."))

A(("h2", "The DTO pattern"))
A(("p",
   "Controllers never accept or return JPA entities. Separate request and response "
   "objects sit in between. This is not ceremony &mdash; it prevents four concrete problems:"))
A(("ul", [
    "**Over-exposure.** A `User` entity has a `password` field. Serialising the entity leaks the hash. A `UserResponse` simply does not have that field.",
    "**Mass assignment.** If a request bound straight onto an entity, a caller could set `role` or `id` by adding them to the JSON.",
    "**Lazy-loading explosions.** Serialising an entity walks its lazy relationships and can trigger a cascade of queries, or fail outside a transaction.",
    "**Coupling.** With DTOs, renaming a column does not silently change your public API.",
]))

A(("h2", "MapStruct"))
A(("p",
   "Writing entity-to-DTO conversion by hand is tedious and easy to get wrong. MapStruct "
   "generates the mapper **at compile time** from an annotated interface. Because it "
   "generates plain Java, there is no reflection at runtime, and an unmapped field is a "
   "*compile error* rather than a silent null in production."))
A(("p",
   "One practical gotcha worth knowing: MapStruct and Lombok both run as annotation "
   "processors, and MapStruct can run before Lombok has generated the getters it needs. "
   "The fix is the `lombok-mapstruct-binding` dependency, which orders them correctly."))

A(("h2", "Bean Validation and global exception handling"))
A(("p",
   "Constraints such as `@NotBlank`, `@Email` and `@Size` are declared on the request "
   "DTOs and enforced by `@Valid` on the controller parameter. A violation throws "
   "`MethodArgumentNotValidException`, which a `@RestControllerAdvice` converts into a "
   "**400** carrying a field-by-field map of what failed."))
A(("p",
   "That advice class is the single place where exceptions become HTTP responses. Domain "
   "exceptions map to intentional status codes; anything unrecognised is logged in full "
   "on the server and returned to the client as a **generic** message."))
A(("key", "Never return raw exception messages to clients",
   "An earlier version of this project returned `ex.getMessage()` and printed stack "
   "traces on the 500 path. That leaks class names, SQL fragments, file paths and "
   "library versions &mdash; a free reconnaissance report for an attacker. The rule is: "
   "**log everything server-side, tell the client only what it needs.**"))

# --- 4.3 data --------------------------------------------------------------

A(("h1", "4.3  JPA, Hibernate and PostgreSQL"))

A(("h2", "ORM and the persistence context"))
A(("p",
   "JPA is the specification, Hibernate the implementation. Within a transaction, "
   "Hibernate maintains a **persistence context**: a first-level cache of managed "
   "entities. Changing a managed entity's field is enough &mdash; at commit Hibernate "
   "compares against its snapshot and writes an `UPDATE` automatically. That is **dirty "
   "checking**, and it is why several service methods in this project modify an entity "
   "without calling `save()`."))

A(("h2", "Lazy loading and the N+1 problem"))
A(("p",
   "Relationships here are `FetchType.LAZY`: a `Product` does not load its "
   "`ProductCategory` until the field is touched. The classic failure is **N+1**: fetch "
   "100 products with one query, then touch each one's category and issue 100 more. The "
   "fixes are a `JOIN FETCH` query or an entity graph."))
A(("p",
   "The related trap is `LazyInitializationException` &mdash; touching a lazy field after "
   "the transaction has closed. This project sets `open-in-view: false`, which *disables* "
   "the crutch that hides the problem. That is intentional: the default leaves the "
   "persistence context open for the whole request, which hides N+1 problems until "
   "production. Turning it off forces you to load what you need inside the service layer."))

A(("h2", "ddl-auto and why it is a development-only convenience"))
A(("p", "`spring.jpa.hibernate.ddl-auto=update` makes Hibernate reshape the schema to match the entities at start-up. It is convenient and genuinely dangerous:"))
A(("ul", [
    "It **adds** columns and tables but will not drop or narrow anything, so the schema silently drifts from the model.",
    "It will not reliably alter an existing column's type or length. In this project widening the refresh-token column from 255 to 1024 characters required recreating the table &mdash; `update` would not do it.",
    "There is no version history, no review, and no rollback.",
]))
A(("p", "**The production answer is Flyway or Liquibase**: versioned, reviewed migration scripts checked into the repository. Naming that unprompted is a strong signal."))

A(("h2", "Soft delete"))
A(("p",
   "Products and categories are never physically deleted; an `active` flag is set to "
   "false and queries filter on it. The reason is specific to this domain: **the ledger "
   "history is immutable and permanent**. If a product row were destroyed, its on-chain "
   "history would reference an identifier the catalogue could no longer explain. Soft "
   "delete keeps the two consistent."))

A(("h2", "UUID primary keys"))
A(("p", "Every entity uses a UUID rather than an auto-incrementing integer:"))
A(("ul", [
    "**They do not leak information.** Sequential IDs reveal how many products you have and allow trivial enumeration of `/products/1`, `/products/2`.",
    "**They are generated without the database**, which matters when identifiers must be created across independent services.",
    "**The cost** is 16 bytes instead of 4 and poorer index locality for random UUIDs. At this scale it is irrelevant; at very large scale you would consider time-ordered UUIDv7.",
]))

# --- 4.4 blockchain --------------------------------------------------------

A(("h1", "4.4  Blockchain fundamentals"))

A(("h2", "What a blockchain actually is"))
A(("p",
   "A blockchain is an **append-only ledger, replicated across multiple parties, where "
   "each block contains a cryptographic hash of the previous block**. That chaining is "
   "what makes it tamper-evident: altering an old transaction changes its block's hash, "
   "which invalidates every subsequent block, and every other participant still holds "
   "the original chain."))
A(("code", """
  +-----------+      +-----------+      +-----------+
  |  Block 41 |      |  Block 42 |      |  Block 43 |
  |  hash(40) |<-----|  hash(41) |<-----|  hash(42) |
  |  txs...   |      |  txs...   |      |  txs...   |
  +-----------+      +-----------+      +-----------+

  change anything in block 41  ->  its hash changes
                               ->  block 42's back-pointer no longer matches
                               ->  the tampering is immediately visible
"""))
A(("key", "Tamper-evident, not tamper-proof",
   "A blockchain does not make data impossible to change. It makes changes **impossible "
   "to hide**. That is a meaningful and often misunderstood difference, and stating it "
   "precisely marks you out from candidates who describe blockchains as \"unhackable\"."))

A(("h2", "Public versus permissioned"))
A(("table", [
    ["", "Public (Bitcoin, Ethereum)", "Permissioned (Hyperledger Fabric)"],
    ["Who can join", "Anyone, anonymously", "Only identities issued by a member organisation"],
    ["Consensus", "Proof of work / proof of stake", "Ordering service (Raft) plus endorsement policy"],
    ["Cost per write", "A transaction fee in cryptocurrency", "None"],
    ["Throughput", "Tens of transactions per second", "Thousands"],
    ["Finality", "Probabilistic &mdash; wait for confirmations", "Immediate once committed"],
    ["Data visibility", "Public to the world", "Restricted to channel members"],
], [14, 43, 43]))

A(("h2", "Why Fabric and not Ethereum &mdash; be ready for this"))
A(("p", "Four reasons, in order of strength:"))
A(("ol", [
    "**The participants are known and vetted.** A supply chain is a set of contracted businesses, not anonymous strangers. The expensive machinery of public blockchains exists to establish trust between parties who cannot identify each other &mdash; a problem this domain does not have.",
    "**Confidentiality is a requirement, not a nice-to-have.** A manufacturer's shipment volumes and distributor relationships are commercially sensitive. On a public chain they would be visible to competitors forever.",
    "**Cost and throughput.** Registering millions of products means millions of writes. On a public chain each carries a fee and contends for block space.",
    "**No cryptocurrency.** Fabric has no native token, so the system needs no exchange, wallet or treasury operation to function.",
]))

A(("h2", "Consensus"))
A(("p",
   "Public chains use consensus to answer *who may append next* among untrusted, "
   "anonymous parties &mdash; proof of work makes appending expensive so that rewriting "
   "history becomes infeasible. Fabric does not need this, because participants are "
   "authenticated. Instead it separates concerns: an **ordering service** running the "
   "Raft protocol decides transaction order, and an **endorsement policy** decides how "
   "many organisations must agree a transaction is valid. No mining, no wasted "
   "electricity, and immediate finality."))

# --- 4.5 fabric ------------------------------------------------------------

A(("h1", "4.5  Hyperledger Fabric in depth"))

A(("h2", "The components"))
A(("table", [
    ["Component", "What it is"],
    ["**Peer**", "A node holding a copy of the ledger and running chaincode. Endorsing peers simulate transactions; committing peers validate and append them."],
    ["**Orderer**", "Sequences transactions into blocks and distributes them. Runs Raft. Does not execute chaincode and never sees the world state."],
    ["**Channel**", "A private sub-ledger between a defined set of organisations. Members of one channel cannot see another's data &mdash; the primary confidentiality mechanism."],
    ["**Organisation**", "A member entity with its own peers and its own certificate authority."],
    ["**MSP**", "Membership Service Provider &mdash; maps X.509 certificates to organisations and roles. It is how Fabric knows *who* signed something."],
    ["**Chaincode**", "The smart contract: the business logic every peer executes. Ours is Go."],
    ["**Fabric CA**", "Issues the X.509 certificates that constitute identity on the network."],
], [20, 80]))

A(("h2", "World state versus the ledger"))
A(("p", "This distinction is very commonly asked, and the two are often confused:"))
A(("ul", [
    "**The blockchain** is the immutable, append-only chain of blocks &mdash; the complete history of every transaction ever committed.",
    "**The world state** is a key-value database (LevelDB or CouchDB) holding only the *current* value of each key. It is a cache, derivable by replaying the whole chain, that exists so queries do not have to.",
]))
A(("p",
   "So `VerifyProduct` reads the world state and is fast. `GetProductHistory` calls "
   "`GetHistoryForKey`, which walks the blockchain itself to reconstruct every past value "
   "of that key &mdash; that is where the immutable audit trail actually comes from."))

A(("h2", "The transaction lifecycle"))
A(("p", "Fabric uses an **execute-order-validate** model, unlike the order-execute model of most blockchains. Know these five steps:"))
A(("code", """
  1. PROPOSE   client builds a signed proposal and sends it to endorsing peers

  2. ENDORSE   each peer SIMULATES the chaincode against its world state.
               Nothing is written. It returns a read-write set plus a signature.
                 read set  = keys read, and their versions
                 write set = keys the transaction would write, and new values

  3. ORDER     client sends the endorsed transaction to the ordering service,
               which sequences it into a block with others

  4. VALIDATE  every peer independently checks:
                 - does the endorsement satisfy the endorsement policy?
                 - do the read-set versions still match the world state?
                   (if not, another transaction changed them first -> MVCC conflict)

  5. COMMIT    the block is appended to the chain. Valid transactions update
               the world state; invalid ones are still recorded, marked invalid
"""))
A(("key", "Invalid transactions are kept",
   "A transaction that fails validation is still written into the block, flagged invalid. "
   "The ledger records what was *attempted*, not merely what succeeded. That is a "
   "genuinely useful property for an audit trail, and a detail that impresses when "
   "mentioned."))

A(("h2", "MVCC conflicts"))
A(("p",
   "Because endorsement simulates against a snapshot, two transactions modifying the "
   "same key concurrently will conflict: the second fails validation at step 4 because "
   "the key's version changed after it read it. This is optimistic concurrency control, "
   "and the standard response is to retry. It is a favourite examiner question about "
   "Fabric."))

A(("h2", "Chaincode must be deterministic"))
A(("p",
   "Every endorsing peer executes the chaincode independently, and their read-write sets "
   "must match exactly. Anything non-deterministic breaks endorsement:"))
A(("ul", [
    "**`time.Now()` is forbidden.** Two peers would produce different timestamps and disagree. This project calls `ctx.GetStub().GetTxTimestamp()`, which returns the *transaction's* timestamp &mdash; identical on every peer.",
    "Random numbers, network calls, and iteration over an unordered Go map are all unsafe for the same reason.",
]))
A(("tip", "A high-value detail",
   "Being able to say \"our chaincode uses the transaction timestamp rather than the "
   "system clock, because chaincode must be deterministic across endorsing peers\" "
   "demonstrates real understanding of Fabric's execution model rather than surface "
   "familiarity."))

A(("h2", "The ZeroFake chaincode"))
A(("p", "One asset type, four transactions:"))
A(("code", """
type ProductAsset struct {
    ProductID        string    // the key
    ManufacturerID   string
    CurrentOwnerID   string
    CurrentOwnerRole string    // MANUFACTURER | WAREHOUSE | DISTRIBUTOR
                               // | RETAILER   | CUSTOMER
    ProductStatus    string    // REGISTERED | IN_TRANSIT | DELIVERED
    IsVerified       bool
    CreatedAt        string
    UpdatedAt        string
}

RegisterProduct(productID, manufacturerID)      -> writes a new asset
TransferOwnership(id, from, to, newRole)        -> validates and updates owner
VerifyProduct(productID)                        -> read-only, returns the asset
GetProductHistory(productID)                    -> GetHistoryForKey, full history
"""))
A(("p",
   "Note that `RegisterProduct` and `TransferOwnership` write and must be *submitted*, "
   "while `VerifyProduct` and `GetProductHistory` only read and are *evaluated*. The "
   "distinction is important: an evaluate goes to a single peer and commits nothing, so "
   "it is far cheaper and does not consume block space. Sending a read through the write "
   "path would be a real design error."))

A(("h2", "The Fabric Gateway client"))
A(("p",
   "Fabric 2.4 introduced the Gateway, which moves the complexity of collecting "
   "endorsements from many peers into the peer itself. The client connects to one "
   "gateway peer, which fans out on its behalf. The older `fabric-sdk-java` required the "
   "client to know the whole topology and is now deprecated."))

# --- 4.6 fraud -------------------------------------------------------------

A(("h1", "4.6  Fraud detection"))

A(("h2", "The design"))
A(("p",
   "The engine is a single pure function: given the catalogue result, the on-chain state, "
   "the scan history and the batch data, it returns a set of triggered rules. Risk is "
   "then the sum of their weights, capped at 100. Keeping it free of I/O is what makes "
   "it directly unit-testable, and the project has 13 tests over this component alone."))
A(("table", [
    ["Rule", "Risk", "Severity", "Fires when"],
    ["`PRODUCT_NOT_FOUND`", "100", "1", "No catalogue record exists at all"],
    ["`BLOCKCHAIN_MISMATCH`", "100", "2", "Catalogued but absent from the ledger"],
    ["`INVALID_OWNER`", "40", "3", "A custody role scans goods it does not own on-chain"],
    ["`MULTIPLE_LOCATION_SCAN`", "35", "4", "The item has been scanned in another location"],
    ["`DUPLICATE_QR`", "30", "5", "The code has been scanned by a different party"],
    ["`SUSPICIOUS_ACTIVITY`", "15", "6", "Five or more scans within ten minutes"],
    ["`EXPIRED_PRODUCT`", "25", "7", "Every batch of the product is past its expiry date"],
], [30, 10, 12, 48]))
A(("p",
   "**Severity is separate from risk.** When several rules fire, the risk scores add up, "
   "but the *headline finding* reported to the user is the most severe one. \"This "
   "product does not exist\" is a more fundamental statement than \"its batch expired\", "
   "even though both may be true."))
A(("p", "Thresholds: **80 or above is COUNTERFEIT**, **20 or above is SUSPICIOUS**, otherwise **GENUINE**. At 80 or above a fraud report is raised automatically."))

A(("h2", "Why the interesting rules are the behavioural ones"))
A(("p",
   "The first two rules are structural: they catch a product that was never real. But a "
   "competent counterfeiter copies a *genuine* QR code, and then both structural checks "
   "pass, because the identity really is registered."))
A(("p",
   "What that attacker cannot do is prevent the genuine item from also being scanned. "
   "Once two physical objects share one identity, the scan history becomes physically "
   "impossible &mdash; the same item is in two cities, or is being 'first sold' repeatedly. "
   "`DUPLICATE_QR` and `MULTIPLE_LOCATION_SCAN` are what catch the realistic attack, and "
   "the blockchain is what makes their evidence trustworthy."))

A(("h2", "Why rules rather than machine learning"))
A(("ul", [
    "**Explainability.** A rule engine can state exactly why an item was flagged. Rejecting a shipment on the word of an unexplainable model is not commercially or legally viable.",
    "**No training data.** Supervised learning needs labelled examples of confirmed counterfeits. A new platform has none.",
    "**Determinism.** The same inputs always produce the same verdict, which makes it testable and auditable.",
]))
A(("p",
   "The honest future path: the rule engine generates exactly the labelled dataset that "
   "machine learning would need. Once enough confirmed fraud reports exist, an anomaly "
   "detection model could run *alongside* the rules and flag patterns nobody wrote a rule "
   "for &mdash; with the rules retained for explainability."))

# --- 4.7 qr ----------------------------------------------------------------

A(("h1", "4.7  QR codes"))
A(("p",
   "A QR code is a 2D barcode storing bytes in a matrix, with Reed-Solomon error "
   "correction. This project uses level H, which tolerates about 30% of the symbol being "
   "damaged &mdash; appropriate for a label that will be scuffed in transit."))
A(("p", "The payload is **the product UUID and nothing else**. That is a security decision:"))
A(("ul", [
    "Encoding the product name, batch and manufacturer would tempt a client to display that data directly &mdash; and a counterfeiter can write whatever they like into a QR code they print themselves.",
    "By encoding only an opaque identifier, the label becomes useless on its own. Every fact shown to the user is fetched from the catalogue and verified against the ledger.",
]))
A(("key", "The QR code is a pointer, not a proof",
   "This is the single most important thing to say about the QR code, and you should say "
   "it before being asked. Anyone can photocopy a QR code. It carries no authentication "
   "and is not meant to. Its only job is to name which identity to go and verify."))

# --- 4.8 rest --------------------------------------------------------------

A(("h1", "4.8  REST API design"))
A(("p", "The conventions this project follows, and the reasoning:"))
A(("ul", [
    "**Nouns for resources, HTTP verbs for actions.** `POST /api/v1/products`, not `/createProduct`.",
    "**GET is safe, PUT and DELETE are idempotent.** Repeating a PUT leaves the same state; repeating a POST creates a second resource. This is why `register-product` returns **409** on a repeat rather than silently succeeding &mdash; a second ledger registration is not the same as the first.",
    "**Versioned paths** (`/api/v1/`) so the contract can evolve without breaking existing clients.",
    "**Meaningful status codes**, which are the API's error vocabulary.",
]))
A(("table", [
    ["Code", "Meaning in this system"],
    ["200 / 201", "Success; 201 when a resource was created"],
    ["400", "The request itself is malformed or fails validation"],
    ["401", "No valid credentials were presented"],
    ["403", "Authenticated, but this role may not do this"],
    ["404", "The resource does not exist &mdash; and for the ledger, a meaningful business signal"],
    ["409", "Conflict with existing state: duplicate code, or already registered on-chain"],
    ["500", "An unexpected server fault. Should be rare and always investigated"],
    ["502", "A service *we* depend on failed. Not the client's fault, and crucially not a verdict"],
], [14, 86]))

# --- 4.9 docker ------------------------------------------------------------

A(("h1", "4.9  Docker and deployment"))
A(("h2", "Containers versus virtual machines"))
A(("p",
   "A VM virtualises hardware and runs a complete guest operating system. A container "
   "shares the host kernel and isolates only the process, its filesystem and its network, "
   "using kernel namespaces and cgroups. The result starts in milliseconds rather than "
   "minutes and costs megabytes rather than gigabytes."))

A(("h2", "Images, layers and multi-stage builds"))
A(("p",
   "An image is a stack of read-only layers; each Dockerfile instruction adds one, and "
   "unchanged layers are reused from cache. Two consequences shape the Dockerfiles here:"))
A(("ul", [
    "**Dependencies are resolved before the source is copied.** Because a layer's cache is invalidated by any change beneath it, copying `pom.xml` and resolving dependencies *first* means editing a Java file does not re-download the entire dependency tree.",
    "**Multi-stage builds.** The first stage uses a full Maven and JDK image to compile. The second copies only the resulting JAR into a slim JRE image. The build tools, the source and the Maven cache never reach the final image &mdash; smaller, and a much smaller attack surface.",
]))
A(("p", "The services also run as a non-root user, which limits what a compromised process can do inside its container."))

A(("h2", "Compose"))
A(("p", "`docker-compose.yml` declares the services, a shared network, named volumes for the database and QR images, and health checks. Two details worth knowing:"))
A(("ul", [
    "**`depends_on` with `condition: service_healthy`** waits for PostgreSQL to accept connections, not merely for its container to exist. Without the condition, services start before the database is ready and fail.",
    "**`JWT_SECRET` uses the `${VAR:?message}` form**, which makes Compose refuse to start if the variable is unset. It is deliberately impossible to launch this stack on an accidental default secret.",
]))
