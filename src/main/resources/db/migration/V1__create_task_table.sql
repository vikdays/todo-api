CREATE TABLE task (
                      id SERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      deadline DATE,
                      status VARCHAR(50) NOT NULL,
                      priority VARCHAR(50) NOT NULL,
                      created_at DATE NOT NULL,
                      updated_at DATE,
                      is_done BOOLEAN
);