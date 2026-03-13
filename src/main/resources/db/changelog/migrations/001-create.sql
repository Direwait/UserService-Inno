CREATE TABLE IF NOT EXISTS users(
	id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	name VARCHAR(64),
	surname VARCHAR(64),
	birth_date DATE,
	email VARCHAR(64),
	active BOOLEAN DEFAULT true,

	updated_at TIMESTAMP,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS payment_cards(
	id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
	user_id UUID REFERENCES users(id) ON DELETE SET NULL,
	number VARCHAR(16),
	holder VARCHAR(64),
	expiration_date DATE,
	active BOOLEAN DEFAULT true,

	updated_at TIMESTAMP,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


CREATE INDEX IF NOT EXISTS cards_number_ind ON payment_cards(number);
CREATE UNIQUE INDEX IF NOT EXISTS users_email_ind ON users(email);
CREATE INDEX IF NOT EXISTS cards_users_ind ON payment_cards(user_id, active);