ALTER TABLE employee_directory
    ADD COLUMN linked_user_id BIGINT;

ALTER TABLE employee_directory
    ADD CONSTRAINT fk_employee_directory_linked_user
        FOREIGN KEY (linked_user_id) REFERENCES app_users(id) ON DELETE SET NULL;

CREATE UNIQUE INDEX uq_employee_directory_linked_user
    ON employee_directory(linked_user_id);
