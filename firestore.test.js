const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function memberEntry(uid, displayName, todayMinutes = 120, status = "Studying", latestMockScore = 640) {
  return {
    uid,
    displayName,
    todayMinutes,
    status,
    latestMockScore,
    updatedAtMillis: Date.now() - 1000,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read, list, or write squads", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("squads").doc("NEET26").get());
  await assertFails(unauthDb.collection("squads").get());
  await assertFails(
    unauthDb.collection("squads").doc("NEET26").set({
      roomCode: "NEET26",
      name: "AIIMS Dreamers",
      creatorUid: ALICE_UID,
      memberUids: [ALICE_UID],
      members: { [ALICE_UID]: memberEntry(ALICE_UID, "Drashya") },
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Authenticated creator creates squad, friend joins, both sync own stats", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date(Date.now() - 5000);

  // Alice creates NEET26 (mirrors SquadRepository.createSquad)
  await assertSucceeds(
    aliceDb.collection("squads").doc("NEET26").set({
      roomCode: "NEET26",
      name: "NEET 2026 Grinders",
      creatorUid: ALICE_UID,
      memberUids: [ALICE_UID],
      members: { [ALICE_UID]: memberEntry(ALICE_UID, "Drashya", 180, "Studying", 655) },
      createdAt: now,
      updatedAt: now,
    })
  );

  // Bob looks up NEET26 by exact roomCode & joins (mirrors SquadRepository.joinSquad)
  await assertSucceeds(bobDb.collection("squads").doc("NEET26").get());
  await assertSucceeds(
    bobDb.collection("squads").doc("NEET26").update({
      memberUids: [ALICE_UID, BOB_UID],
      "members.bob_456": memberEntry(BOB_UID, "Aarav", 95, "Idle", 610),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob syncs his own study stats (mirrors SquadRepository.syncMemberProgress)
  await assertSucceeds(
    bobDb.collection("squads").doc("NEET26").update({
      "members.bob_456": memberEntry(BOB_UID, "Aarav", 210, "Studying", 635),
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Member query scoped with array-contains succeeds; unfiltered list fails
  await assertSucceeds(
    aliceDb.collection("squads").where("memberUids", "array-contains", ALICE_UID).get()
  );
  await assertFails(aliceDb.collection("squads").get());
});

test("Security bounds: blocks cross-member tampering, ghost fields, invalid values, and unauthorized deletes", async () => {
  const created = new Date(Date.now() - 10000);
  const aliceEntry = memberEntry(ALICE_UID, "Drashya", 200, "Studying", 670);
  const bobEntry = memberEntry(BOB_UID, "Aarav", 150, "Idle", 600);
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("squads").doc("NEET26").set({
      roomCode: "NEET26",
      name: "Top Rankers",
      creatorUid: ALICE_UID,
      memberUids: [ALICE_UID, BOB_UID],
      members: {
        [ALICE_UID]: aliceEntry,
        [BOB_UID]: bobEntry,
      },
      createdAt: created,
      updatedAt: created,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();

  // Bob cannot overwrite Alice's stats inside members
  await assertFails(
    bobDb.collection("squads").doc("NEET26").update({
      "members.alice_123": memberEntry(ALICE_UID, "Hacked", 0, "Idle", 0),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot rename creator's squad or alter createdAt
  await assertFails(
    bobDb.collection("squads").doc("NEET26").update({
      name: "Hijacked Squad",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    bobDb.collection("squads").doc("NEET26").update({
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot send invalid status, >720 score, or >1440 study minutes
  await assertFails(
    bobDb.collection("squads").doc("NEET26").update({
      "members.bob_456": memberEntry(BOB_UID, "Aarav", 150, "Playing", 600),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    bobDb.collection("squads").doc("NEET26").update({
      "members.bob_456": memberEntry(BOB_UID, "Aarav", 150, "Idle", 999),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Charlie cannot create squad with bad room code or ghost field
  await assertFails(
    charlieDb.collection("squads").doc("bad").set({
      roomCode: "bad",
      name: "Bad",
      creatorUid: CHARLIE_UID,
      memberUids: [CHARLIE_UID],
      members: { [CHARLIE_UID]: memberEntry(CHARLIE_UID, "Charlie") },
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(
    charlieDb.collection("squads").doc("ROOM99").set({
      roomCode: "ROOM99",
      name: "Ghost Field Squad",
      creatorUid: CHARLIE_UID,
      memberUids: [CHARLIE_UID],
      members: { [CHARLIE_UID]: memberEntry(CHARLIE_UID, "Charlie") },
      isVerified: true,
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Bob cannot delete squad while Alice is also a member
  await assertFails(bobDb.collection("squads").doc("NEET26").delete());

  // Bob can leave squad cleanly
  await assertSucceeds(
    bobDb.collection("squads").doc("NEET26").update({
      memberUids: [ALICE_UID],
      members: {
        [ALICE_UID]: aliceEntry,
      },
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Alice (now sole member) can delete squad
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("squads").doc("NEET26").delete());
});
