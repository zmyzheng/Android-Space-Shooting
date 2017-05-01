import unittest
from middleware import Middleware
import json
import server


class TestMiddleware(unittest.TestCase):
    def setUp(self):
        self.m = Middleware()

    def test_0_register_user(self):
        self.assertEqual(
            self.m.register_user('test user', '123456'),
            {'status': 'success', 'payload': ''}
        )
        self.assertEqual(
            self.m.register_user('test user1', '123456dafgda'),
            {'status': 'success', 'payload': ''}
        )
        self.assertEqual(
            self.m.register_user('test user1', '123456dafgda'),
            {'status': 'fail', 'payload': 'duplicate name'}
        )

    def test_1_login_user(self):
        self.assertEqual(
            self.m.login_user('testuser', '123456'),
            {'status': 'fail', 'payload': 'wrong username or password'}
        )
        self.assertEqual(
            self.m.login_user('test user', '123456'),
            {'status': 'success', 'payload': ''}
        )
        self.assertEqual(
            self.m.register_user('test user1', '123456dafgda'),
            {'status': 'fail', 'payload': 'duplicate name'}
        )

    def test_2_remove_user(self):
        self.assertEqual(self.m.remove_user('tesser', '123456'), 0)
        self.assertEqual(self.m.remove_user('test user', '123 456'), 0)
        self.assertEqual(self.m.remove_user('test user', '123456'), 1)
        self.assertEqual(self.m.remove_user('test user1', '123456dafgda'), 1)
        self.assertEqual(self.m.remove_user('test user1', '123456dafgda'), 0)

    def test_3_create_game_record(self):
        self.assertEqual(
            self.m.create_game_record('test user', 59), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user', 97), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user1', 51), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user2', 28), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user1', 5), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user', 82), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user1', 73), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user2', 19), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user', 44), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user', 94), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user2', 63), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user', 8), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user1', 79), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user2', 60), {"status": "success", "payload": ""}
        )
        self.assertEqual(
            self.m.create_game_record('test user2', 55), {"status": "success", "payload": ""}
        )

    def test_4_get_game_record_by_user(self):
        records = self.m.get_game_record_by_user('test user')
        self.assertEqual(records['status'], 'success')
        self.assertEqual(len(records['payload']), 6)

        truth = [97, 94, 82, 59, 44, 8]
        for i, re in enumerate(records['payload']):
            self.assertEqual(re[u'username'], u'test user')
            self.assertEqual(re[u'score'], truth[i])

    def test_5_get_best_game_record(self):
        records = self.m.get_best_game_record()
        self.assertEqual(records['status'], 'success')
        self.assertEqual(len(records['payload']), 10)

        truth = [97, 94, 82, 79, 73, 63, 60, 59, 55, 51]
        user = ["", "", "", "1", "1", "2", "2", "", "2", "1"]
        for i, re in enumerate(records['payload']):
            self.assertEqual(re[u'username'], 'test user' + user[i])
            self.assertEqual(re[u'score'], truth[i])

    def test_6_remove_game_record(self):
        self.assertEqual(self.m.remove_game_record('test user'), 6)
        self.assertEqual(self.m.remove_game_record('test user1'), 4)
        self.assertEqual(self.m.remove_game_record('test user2'), 5)
        self.assertEqual(self.m.remove_game_record('test user null'), 0)


class TestServer(unittest.TestCase):

    def setUp(self):
        self.app = server.app.test_client()

    def test_0_register_user(self):
        response = json.loads(
            self.app.post('/register', data=json.dumps({'username': 'test user', 'password': '123456'}),
                          content_type='application/json').data)
        self.assertEqual(
            response,
            {'status': 'success', 'payload': ''}
        )
        response = json.loads(
            self.app.post('/register', data=json.dumps({'username': 'test user1', 'password': '123456dafgda'}),
                          content_type='application/json').data)
        self.assertEqual(
            response,
            {'status': 'success', 'payload': ''}
        )
        response = json.loads(
            self.app.post('/register', data=json.dumps({'username': 'test user1', 'password': '123456dafgda'}),
                          content_type='application/json').data)
        self.assertEqual(
            response,
            {'status': 'fail', 'payload': 'duplicate name'}
        )

    def test_1_login_user(self):
        response = json.loads(
            self.app.post('/login', data=json.dumps({'username': 'testuser', 'password': '123456'}),
                          content_type='application/json').data)
        self.assertEqual(
            response,
            {'status': 'fail', 'payload': 'wrong username or password'}
        )
        response = json.loads(
            self.app.post('/login', data=json.dumps({'username': 'test user', 'password': '123456'}),
                          content_type='application/json').data)
        self.assertEqual(
            response,
            {'status': 'success', 'payload': ''}
        )

        response = json.loads(
            self.app.post('/login', data=json.dumps({'username': 'test user1', 'password': '123456dafgda'}),
                          content_type='application/json').data)
        self.assertEqual(
            response,
            {'status': 'success', 'payload': ''}
        )

    def test_2_create_game_record(self):
        scores = [59, 97, 51, 28, 5, 82, 73, 19, 44, 94, 63, 8, 79, 60, 55]
        users = ['', '', '1', '2', '1', '', '1', '2', '', '', '2', '', '1', '2', '2', ]
        for u, s in zip(users, scores):
            response = json.loads(
                self.app.post('/send_game_data', data=json.dumps(
                    {'username': 'test user' + u, 'score': s}),
                              content_type='application/json').data)
            self.assertEqual(
                response,
                {'status': 'success', 'payload': ''}
            )

    def test_3_get_game_record_by_user(self):
        response = json.loads(self.app.get('/get_personal_record?username=test+user').data)
        self.assertEqual(response['status'], 'success')
        self.assertEqual(len(response['payload']), 6)

        truth = [97, 94, 82, 59, 44, 8]
        for i, re in enumerate(response['payload']):
            self.assertEqual(re[u'username'], u'test user')
            self.assertEqual(re[u'score'], truth[i])

    def test_4_get_best_game_record(self):
        response = json.loads(self.app.get('/get_global_record').data)
        self.assertEqual(response['status'], 'success')
        self.assertEqual(len(response['payload']), 10)
        truth = [97, 94, 82, 79, 73, 63, 60, 59, 55, 51]
        user = ["", "", "", "1", "1", "2", "2", "", "2", "1"]
        for i, re in enumerate(response['payload']):
            self.assertEqual(re[u'username'], 'test user' + user[i])
            self.assertEqual(re[u'score'], truth[i])

    def test_9(self):
        m = Middleware()
        m.remove_user('test user', '123456')
        m.remove_user('test user1', '123456dafgda')

        m.remove_game_record('test user')
        m.remove_game_record('test user1')
        m.remove_game_record('test user2')
        m.remove_game_record('test user null')


if __name__ == '__main__':
    unittest.main()
    # t = TestMiddleware()
    # t.setUp()
    # t.test_3_create_game_record()