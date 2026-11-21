# Moving from the earlier project

This is a separate clean implementation, not an in-place patch. Keep your old folder and Docker volumes. Do not replace its `.env` or database volume.

1. Stop the old project, or choose different host ports for this project.
2. Initialize a new `.env` and start this project's Compose stack.
3. Verify the seeded routes and create fresh driver/citizen accounts.
4. If old business data is needed, export it with MongoDB tools and review field mappings before importing into the new databases. Old IDs may be retained only if every referencing document is mapped consistently. No automatic import script is supplied because the old database state and schema have not been migrated or verified.

API paths now start with `/api/v1`; Basic authentication is replaced with RS256 JWT. `fullName` replaces old account `name`; registration numbers and ordered stop offsets are explicit. Fares now belong to transport-service. Recurring schedules and assigned trips are different collections. Never directly copy old users with unknown password-hash settings into this system; recreate test accounts for the demo.

The source ZIP excludes all installed dependencies, compiled files, private environment files, databases and signing keys. Your friend needs Docker Desktop and their own initialized `.env`.
