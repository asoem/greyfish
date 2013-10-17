CREATE TABLE AGENTS (
        id INT NOT NULL PRIMARY KEY,
        population_name_id SMALLINT NOT NULL,
        activated_at INT NOT NULL,
        simulation_id INT NOT NULL);

CREATE TABLE CHROMOSOME_TREE (
        id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
        child_id INT NOT NULL,
        parent_id INT NOT NULL);

CREATE TABLE NAMES (
        id SMALLINT NOT NULL PRIMARY KEY,
        name VARCHAR(255) UNIQUE NOT NULL);

CREATE TABLE QUANTITATIVE_TRAITS (
        id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
        agent_id INT NOT NULL,
        trait_name_id SMALLINT NOT NULL,
        value REAL NOT NULL);

CREATE TABLE DISCRETE_TRAITS (
        id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,
        agent_id INT NOT NULL, 
        trait_name_id SMALLINT NOT NULL, 
        trait_value_id SMALLINT NOT NULL);

CREATE TABLE AGENT_EVENTS (
        id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
        simulation_step INT NOT NULL, 
        agent_id INT NOT NULL, 
        source_name_id SMALLINT NOT NULL, 
        title_name_id SMALLINT NOT NULL, 
        message VARCHAR(255) NOT NULL, 
        x REAL NOT NULL, 
        y REAL NOT NULL);

CREATE TABLE PROPERTIES (
        id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, 
        type VARCHAR(255) NOT NULL, 
        key VARCHAR(255) NOT NULL, 
        value VARCHAR(255) NOT NULL);

CREATE TABLE SIMULATION (
        id INT NOT NULL PRIMARY KEY,
        name VARCHAR(255) NOT NULL);