import pymongo

def main():
    client = pymongo.MongoClient()
    db = client.db

    db.rooms.remove({})
    db.actuators.remove({})
    db.sensors.remove({})


if __name__ == "__main__":
    main()
